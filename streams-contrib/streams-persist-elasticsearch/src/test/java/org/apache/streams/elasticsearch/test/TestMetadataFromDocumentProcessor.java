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

import org.apache.streams.core.StreamsDatum;
import org.apache.streams.elasticsearch.processor.MetadataFromDocumentProcessor;
import org.apache.streams.jackson.StreamsJacksonMapper;
import org.apache.streams.pojo.json.Activity;
import org.apache.streams.pojo.json.ActivityObject;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.SerializationUtils;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Unit Test for
 * @see org.apache.streams.elasticsearch.processor.MetadataFromDocumentProcessor
 */
public class TestMetadataFromDocumentProcessor {

  private static ObjectMapper MAPPER = StreamsJacksonMapper.getInstance();

  private static final Logger LOGGER = LoggerFactory.getLogger(TestMetadataFromDocumentProcessor.class);

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
    List<String> files = IOUtils.readLines(testActivityFolderStream, StandardCharsets.UTF_8);

    Set<ActivityObject> objects = new HashSet<>();

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
      assertNotNull(resultList);
      assertEquals(1, resultList.size());

      StreamsDatum result = resultList.get(0);
      assertNotNull(result);
      assertNotNull(result.getDocument());
      assertNotNull(result.getId());
      assertNotNull(result.getMetadata());
      assertNotNull(result.getMetadata().get("id"));
      assertNotNull(result.getMetadata().get("type"));

      LOGGER.info("valid: " + activity.getVerb() );
    }

    for( ActivityObject activityObject : objects) {
      LOGGER.info("Object: " + MAPPER.writeValueAsString(activityObject));

      activityObject.setId(activityObject.getObjectType());
      StreamsDatum datum = new StreamsDatum(activityObject);

      List<StreamsDatum> resultList = processor.process(datum);
      assertNotNull(resultList);
      assertEquals(1, resultList.size());

      StreamsDatum result = resultList.get(0);
      assertNotNull(result);
      assertNotNull(result.getDocument());
      assertNotNull(result.getId());
      assertNotNull(result.getMetadata());
      assertNotNull(result.getMetadata().get("id"));
      assertNotNull(result.getMetadata().get("type"));

      LOGGER.info("valid: " + activityObject.getObjectType() );
    }
  }
}
