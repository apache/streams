/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.streams.elasticsearch.test;

import com.carrotsearch.ant.tasks.junit4.dependencies.com.google.common.collect.Lists;
import com.carrotsearch.ant.tasks.junit4.dependencies.com.google.common.collect.Sets;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.commons.io.Charsets;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.SerializationUtils;
import org.apache.streams.core.StreamsDatum;
import org.apache.streams.elasticsearch.processor.DocumentToMetadataProcessor;
import org.apache.streams.elasticsearch.processor.MetadataFromDocumentProcessor;
import org.apache.streams.jackson.StreamsJacksonMapper;
import org.apache.streams.pojo.json.Activity;
import org.apache.streams.pojo.json.ActivityObject;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.util.List;
import java.util.Set;

/**
 * Created by sblackmon on 10/20/14.
 */
public class TestMetadataFromDocumentProcessor {

    private static ObjectMapper MAPPER = StreamsJacksonMapper.getInstance();

    private final static Logger LOGGER = LoggerFactory.getLogger(TestMetadataFromDocumentProcessor.class);

    @Before
    public void prepareTest() {

    }

    @Test
    public void testSerializability() {
        MetadataFromDocumentProcessor processor = new MetadataFromDocumentProcessor();

        MetadataFromDocumentProcessor clone = (MetadataFromDocumentProcessor) SerializationUtils.clone(processor);
    }

    @Test
    public void testMetadataFromDocumentProcessor() throws Exception {

        MetadataFromDocumentProcessor processor = new MetadataFromDocumentProcessor();

        processor.prepare(null);

        InputStream testActivityFolderStream = TestMetadataFromDocumentProcessor.class.getClassLoader()
                .getResourceAsStream("activities");
        List<String> files = IOUtils.readLines(testActivityFolderStream, Charsets.UTF_8);

        Set<ActivityObject> objects = Sets.newHashSet();

        for( String file : files) {
            LOGGER.info("File: " + file );
            InputStream testActivityFileStream = TestMetadataFromDocumentProcessor.class.getClassLoader()
                    .getResourceAsStream("activities/" + file);
            Activity activity = MAPPER.readValue(testActivityFileStream, Activity.class);
            activity.setId(activity.getVerb());
            activity.getAdditionalProperties().remove("$license");

            if( activity.getActor().getObjectType() != null)
                objects.add(activity.getActor());
            if( activity.getObject().getObjectType() != null)
                objects.add(activity.getObject());

            StreamsDatum datum = new StreamsDatum(activity);

            List<StreamsDatum> resultList = processor.process(datum);
            assert(resultList != null);
            assert(resultList.size() == 1);

            StreamsDatum result = resultList.get(0);
            assert(result != null);
            assert(result.getDocument() != null);
            assert(result.getId() != null);
            assert(result.getMetadata() != null);
            assert(result.getMetadata().get("id") != null);
            assert(result.getMetadata().get("type") != null);

            LOGGER.info("valid: " + activity.getVerb() );
        }

        for( ActivityObject activityObject : objects) {
            LOGGER.info("Object: " + MAPPER.writeValueAsString(activityObject));

            activityObject.setId(activityObject.getObjectType());
            StreamsDatum datum = new StreamsDatum(activityObject);

            List<StreamsDatum> resultList = processor.process(datum);
            assert(resultList != null);
            assert(resultList.size() == 1);

            StreamsDatum result = resultList.get(0);
            assert(result != null);
            assert(result.getDocument() != null);
            assert(result.getId() != null);
            assert(result.getMetadata() != null);
            assert(result.getMetadata().get("id") != null);
            assert(result.getMetadata().get("type") != null);

            LOGGER.info("valid: " + activityObject.getObjectType() );
        }
    }
}
