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
import com.google.common.collect.Sets;
import org.apache.streams.core.StreamsDatum;
import org.apache.streams.core.StreamsProcessor;
import org.apache.streams.core.util.DatumUtils;
import org.apache.streams.data.ActivityConverter;
import org.apache.streams.data.DocumentClassifier;
import org.apache.streams.data.util.ActivityUtil;
import org.apache.streams.exceptions.ActivityConversionException;
import org.apache.streams.pojo.json.Activity;
import org.reflections.Reflections;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * ActivityConverterProcessor is a utility processor for converting any datum document
 * to an Activity.
 *
 * By default it will handle string json and objectnode representation of existing Activities.
 *
 * Implementations can add DocumentClassifiers and ActivityConverterResolvers to the processor
 * to ensure additional ActivityConverters will be resolved and applied.
 *
 * A DocumentClassifier's reponsibility is to recognize document formats and label them, using
 * a jackson-compatible POJO class.
 *
 * An ActivityConverterResolver's reponsibility is to identify ActivityConverter implementations
 * capable of converting a raw document associated with that POJO class into an activity.
 *
 */
public class ActivityConverterProcessor implements StreamsProcessor {

    private final static Logger LOGGER = LoggerFactory.getLogger(ActivityConverterProcessor.class);

    private List<DocumentClassifier> classifiers;
    private List<ActivityConverter> converters;

    private ActivityConverterProcessorConfiguration configuration;

    public ActivityConverterProcessor() {
        this.classifiers = Lists.newArrayList();
        this.converters = Lists.newArrayList();
    }

    public ActivityConverterProcessor(ActivityConverterProcessorConfiguration configuration) {
        this.classifiers = Lists.newArrayList();
        this.converters = Lists.newArrayList();
        this.configuration = configuration;
    }

    @Override
    public List<StreamsDatum> process(StreamsDatum entry) {

        List<StreamsDatum> result = Lists.newLinkedList();
        Object document = entry.getDocument();

        try {

            // first determine which classes this document might actually be
            List<Class> detectedClasses = detectClasses(document);

            if( detectedClasses.size() == 0 ) {
                LOGGER.warn("Unable to classify");
                return null;
            } else {
                LOGGER.debug("Classified document as " + detectedClasses);
            }

            // for each of these classes:
            //   use TypeUtil to switch the document to that type
            Map<Class, Object> typedDocs = convertToDetectedClasses(detectedClasses, document);

            if( typedDocs.size() == 0 ) {
                LOGGER.warn("Unable to convert to any detected Class");
                return result;
            }
            else {
                LOGGER.debug("Document has " + typedDocs.size() + " representations: " + typedDocs.toString());
            }

            // for each specified / discovered converter
            for( ActivityConverter converter : converters ) {

                Object typedDoc = typedDocs.get(converter.requiredClass());

                // if the document can be typed as the required class
                if( typedDoc != null ) {

                    StreamsDatum datum = DatumUtils.cloneDatum(entry);

                    // let the converter create activities if it can
                    List<Activity> activities;
                    try {
                        activities = convertToActivity(converter, typedDoc);

                        if( activities != null && activities.size() > 0) {

                            for (Activity activity : activities) {

                                if (activity != null) {

                                    // only accept valid activities
                                    //   this primitive validity check should be replaced with
                                    //   one that applies javax.validation to JSR303 annotations
                                    //   on the Activity json schema once a suitable implementation
                                    //   is found.
                                    if (ActivityUtil.isValid(activity)) {
                                        datum.setDocument(activity);
                                        datum.setId(activity.getId());
                                        result.add(datum);
                                    } else {
                                        LOGGER.debug(converter.getClass().getCanonicalName() + " produced invalid Activity converting " + converter.requiredClass().getClass().getCanonicalName());
                                    }

                                } else {
                                    LOGGER.debug(converter.getClass().getCanonicalName() + " returned null converting " + converter.requiredClass().getClass().getCanonicalName() + " to Activity");
                                }

                            }
                        }
                    } catch( Exception e ) {
                        LOGGER.debug("convertToActivity caught exception " + e.getMessage());
                    }



                }

            }

        } catch( Exception e ) {
            LOGGER.warn("General exception in process! " + e.getMessage());
        } finally {
            return result;
        }

    }

    protected List<Activity> convertToActivity(ActivityConverter converter, Object document) {

        List<Activity> activities = Lists.newArrayList();
        try {
            activities = converter.toActivityList(document);
        } catch (ActivityConversionException e1) {
            LOGGER.debug(converter.getClass().getCanonicalName() + " unable to convert " + converter.requiredClass().getClass().getCanonicalName() + " to Activity");
        }
        return activities;

    }

    protected List<Class> detectClasses(Object document) {

        Set<Class> detectedClasses = Sets.newConcurrentHashSet();
        for( DocumentClassifier classifier : classifiers ) {
            List<Class> detected = classifier.detectClasses(document);
            if( detected != null && detected.size() > 0)
                detectedClasses.addAll(detected);
        }

        return Lists.newArrayList(detectedClasses);
    }

    private Map<Class, Object> convertToDetectedClasses(List<Class> datumClasses, Object document) {

        Map<Class, Object> convertedDocuments = Maps.newHashMap();
        for( Class detectedClass : datumClasses ) {

            Object typedDoc;
            if (detectedClass.isInstance(document))
                typedDoc = document;
            else
                typedDoc = TypeConverterUtil.convert(document, detectedClass);

            if( typedDoc != null )
                convertedDocuments.put(detectedClass, typedDoc);
        }

        return convertedDocuments;
    }

    @Override
    public void prepare(Object configurationObject) {
//        Preconditions.checkArgument(configurationObject instanceof ActivityConverterProcessorConfiguration);
//        ActivityConverterProcessorConfiguration configuration = (ActivityConverterProcessorConfiguration) configurationObject;
        Reflections reflections = new Reflections(new ConfigurationBuilder().setUrls(ClasspathHelper.forManifest()));
        if (configuration.getClassifiers().size() > 0) {
            for( DocumentClassifier classifier : configuration.getClassifiers()) {
                try {
                    this.classifiers.add(classifier);
                } catch (Exception e) {
                    LOGGER.warn("Exception adding " + classifier);
                }
            }
        } else {
            Set<Class<? extends DocumentClassifier>> classifierClasses = reflections.getSubTypesOf(DocumentClassifier.class);
            for (Class classifierClass : classifierClasses) {
                try {
                    this.classifiers.add((DocumentClassifier) classifierClass.newInstance());
                } catch (Exception e) {
                    LOGGER.warn("Exception instantiating " + classifierClass);
                }
            }
        }
        Preconditions.checkArgument(this.classifiers.size() > 0);
        if (configuration.getConverters().size() > 0) {
            for( ActivityConverter converter : configuration.getConverters()) {
                try {
                    this.converters.add(converter);
                } catch (Exception e) {
                    LOGGER.warn("Exception adding " + converter);
                }
            }
        } else {
            Set<Class<? extends ActivityConverter>> converterClasses = reflections.getSubTypesOf(ActivityConverter.class);
            for (Class converterClass : converterClasses) {
                try {
                    this.converters.add((ActivityConverter) converterClass.newInstance());
                } catch (Exception e) {
                    LOGGER.warn("Exception instantiating " + converterClass);
                }
            }
        }
        Preconditions.checkArgument(this.converters.size() > 0);
    }

    @Override
    public void cleanUp() {

    }

};
