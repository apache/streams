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

import org.apache.streams.data.ActivityConverter;
import org.apache.streams.data.DocumentClassifier;
import org.apache.streams.data.util.ActivityUtil;
import org.apache.streams.exceptions.ActivityConversionException;
import org.apache.streams.pojo.json.Activity;

import com.google.common.base.Preconditions;
import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;


/**
 * ActivityConverterUtil converts document into all possible Activity
 * representations based on registered DocumentClassifiers and ActivityConverters.
 *
 * <p/>
 * Implementations and contributed modules may implement DocumentClassifiers
 * and ActivityConverters to translate additional document types into desired
 * Activity formats.
 *
 * <p/>
 * A DocumentClassifier's reponsibility is to recognize document formats and label them,
 * using a jackson-compatible POJO class.
 *
 * <p/>
 * An ActivityConverter's reponsibility is to converting a raw document associated with an
 * incoming POJO class into an activity.
 *
 */
public class ActivityConverterUtil {

  private static final Logger LOGGER = LoggerFactory.getLogger(ActivityConverterUtil.class);

  private static final ActivityConverterUtil INSTANCE = new ActivityConverterUtil();

  public static ActivityConverterUtil getInstance() {
    return INSTANCE;
  }

  public static ActivityConverterUtil getInstance(ActivityConverterProcessorConfiguration configuration) {
    return new ActivityConverterUtil(configuration);
  }

  private List<DocumentClassifier> classifiers = new LinkedList<>();
  private List<ActivityConverter> converters = new LinkedList<>();

  /*
    Use getInstance to get a globally shared thread-safe ActivityConverterUtil,
    rather than call this constructor.  Reflection-based resolution of
    converters across all modules can be slow and should only happen
    once per JVM.
   */
  protected ActivityConverterUtil() {
    configure();
  }

  protected ActivityConverterUtil(ActivityConverterProcessorConfiguration configuration) {
    classifiers = configuration.getClassifiers();
    converters = configuration.getConverters();
    configure();
  }

  /**
   * convert document to activity.
   *
   * @param document document to convert
   * @return result
   */
  public List<Activity> convert(Object document) {

    List<Activity> result = new ArrayList<>();

    List<Class> detectedClasses = detectClasses(document);

    if ( detectedClasses.size() == 0 ) {
      LOGGER.warn("Unable to classify");
      return null;
    } else {
      LOGGER.debug("Classified document as " + detectedClasses);
    }

    // for each of these classes:
    //   use TypeUtil to switch the document to that type
    Map<Class, Object> typedDocs = convertToDetectedClasses(detectedClasses, document);

    if ( typedDocs.size() == 0 ) {
      LOGGER.warn("Unable to convert to any detected Class");
      return null;
    } else {
      LOGGER.debug("Document has " + typedDocs.size() + " representations: " + typedDocs.toString());
    }

    // for each specified / discovered converter
    for ( ActivityConverter converter : converters ) {

      Object typedDoc = typedDocs.get(converter.requiredClass());

      List<Activity> activities = applyConverter(converter, typedDoc);

      result.addAll(activities);
    }

    return result;
  }

  protected List<Activity> applyConverter(ActivityConverter converter, Object typedDoc) {

    List<Activity> activities = new ArrayList<>();
    // if the document can be typed as the required class
    if ( typedDoc != null ) {

      // let the converter create activities if it can
      try {
        activities = convertToActivity(converter, typedDoc);
      } catch ( Exception ex ) {
        LOGGER.debug("convertToActivity caught exception " + ex.getMessage());
      }

    }
    return activities;
  }

  protected List<Activity> convertToActivity(ActivityConverter converter, Object document) {

    List<Activity> activities = new ArrayList<>();
    try {
      activities = converter.toActivityList(document);
    } catch (ActivityConversionException e1) {
      LOGGER.debug(converter.getClass().getCanonicalName() + " unable to convert " + converter.requiredClass().getClass().getCanonicalName() + " to Activity");
    }

    for (Activity activity : activities) {

      if (activity != null) {

        // only accept valid activities
        //   this primitive validity check should be replaced with
        //   one that applies javax.validation to JSR303 annotations
        //   on the Activity json schema once a suitable implementation
        //   is found.
        if (!ActivityUtil.isValid(activity)) {
          activities.remove(activity);
          LOGGER.debug(converter.getClass().getCanonicalName() + " produced invalid Activity converting " + converter.requiredClass().getClass().getCanonicalName());
        }

      } else {
        LOGGER.debug(converter.getClass().getCanonicalName() + " returned null converting " + converter.requiredClass().getClass().getCanonicalName() + " to Activity");
      }

    }
    return activities;

  }

  protected List<Class> detectClasses(Object document) {

    // ConcurrentHashSet is preferable, but it's only in guava 15+
    // spark 1.5.0 uses guava 14 so for the moment this is the workaround
    // Set<Class> detectedClasses = new ConcurrentHashSet();
    Set<Class> detectedClasses = Collections.newSetFromMap(new ConcurrentHashMap<Class, Boolean>());

    for ( DocumentClassifier classifier : classifiers ) {
      try {
        List<Class> detected = classifier.detectClasses(document);
        if (detected != null && detected.size() > 0) {
          detectedClasses.addAll(detected);
        }
      } catch ( Exception ex ) {
        LOGGER.warn("{} failed in method detectClasses - ()", classifier.getClass().getCanonicalName(), ex);
      }
    }

    return new ArrayList<>(detectedClasses);
  }

  private Map<Class, Object> convertToDetectedClasses(List<Class> datumClasses, Object document) {

    Map<Class, Object> convertedDocuments = new HashMap<>();
    for ( Class detectedClass : datumClasses ) {

      Object typedDoc;
      if (detectedClass.isInstance(document)) {
        typedDoc = document;
      } else {
        typedDoc = TypeConverterUtil.getInstance().convert(document, detectedClass);
      }

      if ( typedDoc != null ) {
        convertedDocuments.put(detectedClass, typedDoc);
      }
    }

    return convertedDocuments;
  }

  /**
   * configure ActivityConverterUtil.
   */
  public void configure() {
    Reflections reflections = new Reflections(new ConfigurationBuilder()
        .setUrls(ClasspathHelper.forPackage("org.apache.streams"))
        .setScanners(new SubTypesScanner()));
    if ( classifiers.size() == 0) {
      Set<Class<? extends DocumentClassifier>> classifierClasses = reflections.getSubTypesOf(DocumentClassifier.class);
      for (Class classifierClass : classifierClasses) {
        try {
          this.classifiers.add((DocumentClassifier) classifierClass.newInstance());
        } catch (Exception ex) {
          LOGGER.warn("Exception instantiating " + classifierClass);
        }
      }
    }
    Preconditions.checkArgument(classifiers.size() > 0);
    if ( converters.size() == 0) {
      Set<Class<? extends ActivityConverter>> converterClasses = reflections.getSubTypesOf(ActivityConverter.class);
      for (Class converterClass : converterClasses) {
        try {
          this.converters.add((ActivityConverter) converterClass.newInstance());
        } catch (Exception ex) {
          LOGGER.warn("Exception instantiating " + converterClass);
        }
      }
    }
    Preconditions.checkArgument(this.converters.size() > 0);
  }
}
