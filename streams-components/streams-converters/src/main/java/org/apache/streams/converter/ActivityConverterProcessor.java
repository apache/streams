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
 *
 */
public class ActivityConverterProcessor extends TypeConverterProcessor {

    private final static Logger LOGGER = LoggerFactory.getLogger(ActivityConverterProcessor.class);

    private List<DocumentClassifier> classifiers;
    private List<ActivityConverterResolver> resolvers;

    public ActivityConverterProcessor() {
        super(Activity.class);
        this.classifiers = Lists.newArrayList((DocumentClassifier)BaseDocumentClassifier.getInstance());
        this.resolvers = Lists.newArrayList((ActivityConverterResolver) BaseActivityConverterResolver.getInstance());
    }

    public ActivityConverterProcessor(List<DocumentClassifier> classifiers, List<ActivityConverterResolver> factories) {
        super(Activity.class);
        this.classifiers = classifiers;
        this.resolvers = factories;
        this.classifiers.add(BaseDocumentClassifier.getInstance());
        this.resolvers.add(BaseActivityConverterResolver.getInstance());
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

            // This implementation is primitive, greedy, takes first it can resolve
            Class converterClass = null;
            for( ActivityConverterResolver resolver : resolvers ) {
                converterClass = resolver.bestSerializer(datumClass);
                if( converterClass != null )
                    break;
            }

            ActivityConverter converter = ActivityConverterFactory.getInstance(converterClass);

            Object typedDoc;
            if( datumClass.isInstance(inDoc) )
                typedDoc = inDoc;
            else
                typedDoc = TypeConverterUtil.convert(inDoc, datumClass, mapper);

            Activity activity = converter.deserialize(typedDoc);

            entry.setDocument(activity);

            result.add(entry);

        } catch( Throwable e ) {
            LOGGER.warn("Unable to serialize!", e.getMessage());
        }

        return result;
    }

    @Override
    public void prepare(Object configurationObject) {
        super.prepare(configurationObject);
        Preconditions.checkArgument(classifiers.size() > 0);
        Preconditions.checkArgument(resolvers.size() > 0);
    }

};
