/*
Licensed to the Apache Software Foundation (ASF) under one
or more contributor license agreements.  See the NOTICE file
distributed with this work for additional information
regarding copyright ownership.  The ASF licenses this file
to you under the Apache License, Version 2.0 (the
"License"); you may not use this file except in compliance
with the License.  You may obtain a copy of the License at

  http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing,
software distributed under the License is distributed on an
"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
KIND, either express or implied.  See the License for the
specific language governing permissions and limitations
under the License.
*/

package org.apache.streams.converter;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.streams.data.ActivityObjectConverter;
import org.apache.streams.data.DocumentClassifier;
import org.apache.streams.data.util.ActivityUtil;
import org.apache.streams.exceptions.ActivityConversionException;
import org.apache.streams.pojo.json.Activity;
import org.apache.streams.pojo.json.ActivityObject;
import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * ActivityObjectConverterUtil converts document into all possible ActivityObject
 * representations based on registered DocumentClassifiers and ActivityObjectConverters.
 *
 * Implementations and contributed modules may implement DocumentClassifiers
 * and ActivityObjectConverters to translate additional document types into desired
 * ActivityObject formats.
 *
 * A DocumentClassifier's reponsibility is to recognize document formats and label them,
 * using a jackson-compatible POJO class.
 *
 * An ActivityObjectConverter's reponsibility is to converting a raw document associated with an
 * incoming POJO class into an activity object.
 *
 */
public class ActivityObjectConverterUtil {

    private final static Logger LOGGER = LoggerFactory.getLogger(ActivityObjectConverterUtil.class);

    private static final ActivityObjectConverterUtil INSTANCE = new ActivityObjectConverterUtil();

    public static ActivityObjectConverterUtil getInstance() {
        return INSTANCE;
    }

    public static ActivityObjectConverterUtil getInstance(ActivityObjectConverterProcessorConfiguration configuration) {
        return new ActivityObjectConverterUtil(configuration);
    }

    private List<DocumentClassifier> classifiers = Lists.newLinkedList();
    private List<ActivityObjectConverter> converters = Lists.newLinkedList();

    /*
      Use getInstance to get a globally shared thread-safe ActivityConverterUtil,
      rather than call this constructor.  Reflection-based resolution of
      converters across all modules can be slow and should only happen
      once per JVM.
     */
    protected ActivityObjectConverterUtil() {
        configure();
    }

    protected ActivityObjectConverterUtil(ActivityObjectConverterProcessorConfiguration configuration) {
        classifiers = configuration.getClassifiers();
        converters = configuration.getConverters();
        configure();
    }

    public synchronized ActivityObject convert(Object document) {

        List<Class> detectedClasses = detectClasses(document);

        if (detectedClasses.size() == 0) {
            LOGGER.warn("Unable to classify");
            return null;
        } else {
            LOGGER.debug("Classified document as " + detectedClasses);
        }

        // for each of these classes:
        //   use TypeUtil to switch the document to that type
        Map<Class, Object> typedDocs = convertToDetectedClasses(detectedClasses, document);

        if (typedDocs.size() == 0) {
            LOGGER.warn("Unable to convert to any detected Class");
            return null;
        } else {
            LOGGER.debug("Document has " + typedDocs.size() + " representations: " + typedDocs.toString());
        }

        Map<Class, ActivityObject> convertedDocs = new HashMap<>();

        // for each specified / discovered converter
        for (ActivityObjectConverter converter : converters) {

            Class requiredClass = converter.requiredClass();

            Object typedDoc = typedDocs.get(requiredClass);

            ActivityObject activityObject = applyConverter(converter, typedDoc);

            convertedDocs.put(requiredClass, activityObject);
        }

        ActivityObject result = deepestDescendant(convertedDocs);

        return result;
    }

    protected ActivityObject applyConverter(ActivityObjectConverter converter, Object typedDoc) {

        ActivityObject activityObject = null;
        // if the document can be typed as the required class
        if (typedDoc != null) {

            // let the converter create activities if it can
            try {
                activityObject = convertToActivityObject(converter, typedDoc);
            } catch (Exception e) {
                LOGGER.debug("convertToActivity caught exception " + e.getMessage());
            }

        }
        return activityObject;
    }

    protected ActivityObject convertToActivityObject(ActivityObjectConverter converter, Object document) {

        ActivityObject activityObject = null;
        try {
            activityObject = converter.toActivityObject(document);
        } catch (ActivityConversionException e1) {
            LOGGER.debug(converter.getClass().getCanonicalName() + " unable to convert " + converter.requiredClass().getClass().getCanonicalName() + " to Activity");
        }

        return activityObject;

    }

    protected List<Class> detectClasses(Object document) {

        // ConcurrentHashSet is preferable, but it's only in guava 15+
        // spark 1.5.0 uses guava 14 so for the moment this is the workaround
        // Set<Class> detectedClasses = new ConcurrentHashSet();
        Set<Class> detectedClasses = Collections.newSetFromMap(new ConcurrentHashMap<Class, Boolean>());
        for (DocumentClassifier classifier : classifiers) {
            try {
                List<Class> detected = classifier.detectClasses(document);
                if (detected != null && detected.size() > 0)
                    detectedClasses.addAll(detected);
            } catch (Exception e) {
                LOGGER.warn("{} failed in method detectClasses - ()", classifier.getClass().getCanonicalName(), e);
            }
        }

        return Lists.newArrayList(detectedClasses);
    }

    private Map<Class, Object> convertToDetectedClasses(List<Class> datumClasses, Object document) {

        Map<Class, Object> convertedDocuments = Maps.newHashMap();
        for (Class detectedClass : datumClasses) {

            Object typedDoc;
            if (detectedClass.isInstance(document))
                typedDoc = document;
            else
                typedDoc = TypeConverterUtil.getInstance().convert(document, detectedClass);

            if (typedDoc != null)
                convertedDocuments.put(detectedClass, typedDoc);
        }

        return convertedDocuments;
    }

    public void configure() {
        Reflections reflections = new Reflections(new ConfigurationBuilder()
                .setUrls(ClasspathHelper.forPackage("org.apache.streams"))
                .setScanners(new SubTypesScanner()));
        if (classifiers.size() == 0) {
            Set<Class<? extends DocumentClassifier>> classifierClasses = reflections.getSubTypesOf(DocumentClassifier.class);
            for (Class classifierClass : classifierClasses) {
                try {
                    this.classifiers.add((DocumentClassifier) classifierClass.newInstance());
                } catch (Exception e) {
                    LOGGER.warn("Exception instantiating " + classifierClass);
                }
            }
        }
        Preconditions.checkArgument(classifiers.size() > 0);
        if (converters.size() == 0) {
            Set<Class<? extends ActivityObjectConverter>> converterClasses = reflections.getSubTypesOf(ActivityObjectConverter.class);
            for (Class converterClass : converterClasses) {
                try {
                    this.converters.add((ActivityObjectConverter) converterClass.newInstance());
                } catch (Exception e) {
                    LOGGER.warn("Exception instantiating " + converterClass);
                }
            }
        }
        Preconditions.checkArgument(this.converters.size() > 0);
    }

    private boolean isAncestor(Class possibleDescendant, Class possibleAncestor) {
        if (possibleDescendant.equals(Object.class))
            return false;
        if (possibleDescendant.getSuperclass().equals(possibleAncestor))
            return true;
        else return isAncestor(possibleDescendant.getSuperclass(), possibleAncestor);
    }

    // prefer the most specific ActivityObject sub-class returned by all converters
    private ActivityObject deepestDescendant(Map<Class, ActivityObject> map) {

        ActivityObject result = null;

        for( Map.Entry<Class, ActivityObject> entry : map.entrySet()) {
            if( entry.getKey() != null ) {
                if (result == null)
                    result = entry.getValue();
                else if (isAncestor(entry.getKey(), result.getClass()))
                    result = entry.getValue();
            }
        }

        return result;
    }


}
