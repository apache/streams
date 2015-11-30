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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.google.common.collect.Lists;
import org.apache.commons.io.Charsets;
import org.apache.commons.io.IOUtils;
import org.apache.streams.core.StreamsDatum;
import org.apache.streams.elasticsearch.ElasticsearchPersistUpdater;
import org.apache.streams.elasticsearch.ElasticsearchPersistWriter;
import org.apache.streams.elasticsearch.ElasticsearchWriterConfiguration;
import org.apache.streams.jackson.StreamsJacksonMapper;
import org.apache.streams.pojo.json.Activity;
import org.apache.streams.pojo.json.Actor;
import org.elasticsearch.index.query.FilterBuilders;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.test.ElasticsearchIntegrationTest;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.util.*;

/**
 * Created by sblackmon on 10/20/14.
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@ElasticsearchIntegrationTest.ClusterScope(scope= ElasticsearchIntegrationTest.Scope.TEST, numNodes=1)
public class TestElasticsearchPersistWriterIT extends ElasticsearchIntegrationTest {

    protected String TEST_INDEX = "TestElasticsearchPersistWriter".toLowerCase();

    private final static Logger LOGGER = LoggerFactory.getLogger(TestElasticsearchPersistWriterIT.class);

    private static ObjectMapper MAPPER = StreamsJacksonMapper.getInstance();

    protected ElasticsearchWriterConfiguration testConfiguration;

    @Before
    public void prepareTest() {

        testConfiguration = new ElasticsearchWriterConfiguration();
        testConfiguration.setHosts(Lists.newArrayList("localhost"));
        testConfiguration.setClusterName(cluster().getClusterName());
        testConfiguration.setIndex("writer");
        testConfiguration.setType("activity");

    }

    @Test
    public void testPersist() throws Exception {
        testPersistWriter();
        testPersistUpdater();
    }

    void testPersistWriter() throws Exception {

       assert(!indexExists(TEST_INDEX));

       ElasticsearchPersistWriter testPersistWriter = new ElasticsearchPersistWriter(testConfiguration);
       testPersistWriter.prepare(null);

       InputStream testActivityFolderStream = TestElasticsearchPersistWriterIT.class.getClassLoader()
               .getResourceAsStream("activities");
       List<String> files = IOUtils.readLines(testActivityFolderStream, Charsets.UTF_8);

       for( String file : files) {
           LOGGER.info("File: " + file );
           InputStream testActivityFileStream = TestElasticsearchPersistWriterIT.class.getClassLoader()
                   .getResourceAsStream("activities/" + file);
           Activity activity = MAPPER.readValue(testActivityFileStream, Activity.class);
           StreamsDatum datum = new StreamsDatum(activity, activity.getVerb());
           testPersistWriter.write( datum );
           LOGGER.info("Wrote: " + activity.getVerb() );
       }

       testPersistWriter.cleanUp();

       flushAndRefresh();

       long count = client().count(client().prepareCount().request()).actionGet().getCount();

       assert(count == 89);

    }

    void testPersistUpdater() throws Exception {

        long count = client().count(client().prepareCount().request()).actionGet().getCount();

        ElasticsearchPersistUpdater testPersistUpdater = new ElasticsearchPersistUpdater(testConfiguration);
        testPersistUpdater.prepare(null);

        InputStream testActivityFolderStream = TestElasticsearchPersistWriterIT.class.getClassLoader()
                .getResourceAsStream("activities");
        List<String> files = IOUtils.readLines(testActivityFolderStream, Charsets.UTF_8);

        for( String file : files) {
            LOGGER.info("File: " + file );
            InputStream testActivityFileStream = TestElasticsearchPersistWriterIT.class.getClassLoader()
                    .getResourceAsStream("activities/" + file);
            Activity activity = MAPPER.readValue(testActivityFileStream, Activity.class);
            Activity update = new Activity();
            update.setAdditionalProperty("updated", Boolean.TRUE);
            update.setAdditionalProperty("str", "str");
            update.setAdditionalProperty("long", 10l);
            update.setActor(
                    (Actor) new Actor()
                    .withAdditionalProperty("updated", Boolean.TRUE)
                    .withAdditionalProperty("double", 10d)
                    .withAdditionalProperty("map",
                            MAPPER.createObjectNode().set("field", MAPPER.createArrayNode().add("item"))));

            StreamsDatum datum = new StreamsDatum(update, activity.getVerb());
            testPersistUpdater.write( datum );
            LOGGER.info("Updated: " + activity.getVerb() );
        }

        testPersistUpdater.cleanUp();

        flushAndRefresh();

        long updated = client().prepareCount().setQuery(
                QueryBuilders.filteredQuery(QueryBuilders.matchAllQuery(),
                        FilterBuilders.existsFilter("updated")
                )
        ).execute().actionGet().getCount();

        LOGGER.info("updated: {}", updated);

        assertEquals(count, updated);

        long actorupdated = client().prepareCount().setQuery(
                QueryBuilders.termQuery("actor.updated", true)
        ).execute().actionGet().getCount();

        LOGGER.info("actor.updated: {}", actorupdated);

        assertEquals(count, actorupdated);

        long strupdated = client().prepareCount().setQuery(
                QueryBuilders.termQuery("str", "str")
        ).execute().actionGet().getCount();

        LOGGER.info("strupdated: {}", strupdated);

        assertEquals(count, strupdated);

        long longupdated = client().prepareCount().setQuery(
                QueryBuilders.rangeQuery("long").from(9).to(11)
        ).execute().actionGet().getCount();

        LOGGER.info("longupdated: {}", longupdated);

        assertEquals(count, longupdated);

        long doubleupdated = client().prepareCount().setQuery(
                QueryBuilders.rangeQuery("long").from(9).to(11)
        ).execute().actionGet().getCount();

        LOGGER.info("doubleupdated: {}", doubleupdated);

        assertEquals(count, doubleupdated);

        long mapfieldupdated = client().prepareCount().setQuery(
                QueryBuilders.termQuery("actor.map.field", "item")
        ).execute().actionGet().getCount();

        LOGGER.info("mapfieldupdated: {}", mapfieldupdated);

        assertEquals(count, mapfieldupdated);

    }
}
