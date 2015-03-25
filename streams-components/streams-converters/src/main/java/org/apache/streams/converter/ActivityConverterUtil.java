package org.apache.streams.converter;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.apache.streams.data.ActivityConverter;
import org.apache.streams.data.DocumentClassifier;
import org.apache.streams.data.util.ActivityUtil;
import org.apache.streams.exceptions.ActivityConversionException;
import org.apache.streams.pojo.json.Activity;
import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by sblackmon on 3/25/15.
 */
public class ActivityConverterUtil {

    private final static Logger LOGGER = LoggerFactory.getLogger(ActivityConverterUtil.class);

    private static final ActivityConverterUtil INSTANCE = new ActivityConverterUtil();

    public static ActivityConverterUtil getInstance(){
        return INSTANCE;
    }

    public static ActivityConverterUtil getInstance(ActivityConverterProcessorConfiguration configuration){
        return new ActivityConverterUtil(configuration);
    }

    private List<DocumentClassifier> classifiers = Lists.newLinkedList();
    private List<ActivityConverter> converters = Lists.newLinkedList();

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
    public List<Activity> convert(Object document) {

        List<Activity> result = Lists.newArrayList();

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
            return null;
        }
        else {
            LOGGER.debug("Document has " + typedDocs.size() + " representations: " + typedDocs.toString());
        }

        // for each specified / discovered converter
        for( ActivityConverter converter : converters ) {

            Object typedDoc = typedDocs.get(converter.requiredClass());

            List<Activity> activities = applyConverter(converter, typedDoc);

            result.addAll(activities);
        }

        return result;
    }

    protected List<Activity> applyConverter(ActivityConverter converter, Object typedDoc) {

        List<Activity> activities = Lists.newArrayList();
        // if the document can be typed as the required class
        if( typedDoc != null ) {

            // let the converter create activities if it can
            try {
                activities = convertToActivity(converter, typedDoc);
            } catch( Exception e ) {
                LOGGER.debug("convertToActivity caught exception " + e.getMessage());
            }

        }
        return activities;
    }

    protected List<Activity> convertToActivity(ActivityConverter converter, Object document) {

        List<Activity> activities = Lists.newArrayList();
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

    public void configure() {
        Reflections reflections = new Reflections(new ConfigurationBuilder()
                .setUrls(ClasspathHelper.forPackage("org.apache.streams"))
                .setScanners(new SubTypesScanner()));
        if ( classifiers.size() == 0) {
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
        if ( converters.size() == 0) {
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
}
