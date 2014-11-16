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
import org.apache.streams.core.StreamsDatum;
import org.apache.streams.data.ActivityConverter;
import org.apache.streams.data.ActivityConverterFactory;
import org.apache.streams.data.ActivityConverterResolver;
import org.apache.streams.data.DocumentClassifier;
import org.apache.streams.pojo.json.Activity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

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
public class ActivityConverterProcessor extends TypeConverterProcessor {

    private final static Logger LOGGER = LoggerFactory.getLogger(ActivityConverterProcessor.class);

    protected ActivityConverterProcessorConfiguration configuration;

    private List<DocumentClassifier> classifiers;
    private List<ActivityConverterResolver> resolvers;

    public ActivityConverterProcessor() {
        super(Activity.class);
        this.classifiers = Lists.newArrayList();
        this.resolvers = Lists.newArrayList();
    }

    public ActivityConverterProcessor(ActivityConverterProcessorConfiguration configuration) {
        super(Activity.class);
        this.configuration = configuration;
        this.classifiers = Lists.newArrayList();
        this.resolvers = Lists.newArrayList();
    }

    @Override
    public List<StreamsDatum> process(StreamsDatum entry) {

        Preconditions.checkArgument(classifiers.size() > 0);
        Preconditions.checkArgument(resolvers.size() > 0);

        List<StreamsDatum> result = Lists.newLinkedList();
        Object inDoc = entry.getDocument();

        try {

            // This implementation is primitive, greedy, takes first it can resolve
            Class datumClass = null;
            for( DocumentClassifier classifier : classifiers ) {
                datumClass = classifier.detectClass(inDoc);
                if( classifier != null )
                    break;
            }

            //Preconditions.checkNotNull(datumClass);
            if( datumClass == null) {
                LOGGER.warn("Unable to classify");
                return result;
            } else {
                LOGGER.debug("Classifies document as " + datumClass.getSimpleName());
            }

            // This implementation is primitive, greedy, takes first it can resolve
            Class converterClass = null;
            for( ActivityConverterResolver resolver : resolvers ) {
                converterClass = resolver.bestSerializer(datumClass);
                if( converterClass != null )
                    break;
            }

            //Preconditions.checkNotNull(converterClass);
            if( converterClass == null) {
                LOGGER.warn("Unable to resolve converterClass");
                return result;
            }
            else {
                LOGGER.debug("Resolved converter: " + converterClass.getSimpleName());
            }

            ActivityConverter converter = ActivityConverterFactory.getInstance(converterClass);

            //Preconditions.checkNotNull(converter);
            if( converter == null) return result;

            Object typedDoc;
            if( datumClass.isInstance(inDoc) )
                typedDoc = inDoc;
            else
                typedDoc = TypeConverterUtil.convert(inDoc, datumClass, mapper);

            //Preconditions.checkNotNull(typedDoc);
            if( typedDoc == null) {
                LOGGER.warn("Unable to convert " + inDoc.getClass().getSimpleName() + " to " + datumClass.getSimpleName());
                return result;
            }

            Activity activity = converter.deserialize(typedDoc);

            //Preconditions.checkNotNull(activity);
            if( activity == null) {
                LOGGER.warn("Unable to convert " + datumClass.getClass().getCanonicalName() + " to Activity");
                return result;
            }

            entry.setDocument(activity);

            result.add(entry);

        } catch( Exception e ) {
            LOGGER.warn("Unable to serialize!  " + e.getMessage());
            e.printStackTrace();
        } finally {
            return result;
        }

    }

    @Override
    public void prepare(Object configurationObject) {
        super.prepare(configurationObject);
        if( configuration != null ) {
            if (configuration.getClassifiers() != null && configuration.getClassifiers().size() > 0)
                this.classifiers.addAll(configuration.getClassifiers());
            if (configuration.getResolvers() != null && configuration.getResolvers().size() > 0)
                this.resolvers.addAll(configuration.getResolvers());
        }
        this.classifiers.add(BaseDocumentClassifier.getInstance());
        this.resolvers.add(BaseActivityConverterResolver.getInstance());
    }

};
