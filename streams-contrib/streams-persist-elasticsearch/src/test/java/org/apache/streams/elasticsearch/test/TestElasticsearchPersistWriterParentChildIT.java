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

import com.carrotsearch.ant.tasks.junit4.dependencies.com.google.common.base.Strings;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.Lists;
import org.apache.commons.io.Charsets;
import org.apache.commons.io.IOUtils;
import org.apache.lucene.queryparser.xml.builders.TermQueryBuilder;
import org.apache.streams.core.StreamsDatum;
import org.apache.streams.elasticsearch.ElasticsearchConfiguration;
import org.apache.streams.elasticsearch.ElasticsearchPersistUpdater;
import org.apache.streams.elasticsearch.ElasticsearchPersistWriter;
import org.apache.streams.elasticsearch.ElasticsearchWriterConfiguration;
import org.apache.streams.jackson.StreamsJacksonMapper;
import org.apache.streams.pojo.json.Activity;
import org.apache.streams.pojo.json.ActivityObject;
import org.elasticsearch.action.admin.indices.template.put.PutIndexTemplateRequestBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.test.ElasticsearchIntegrationTest;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.net.URL;
import java.util.List;
import java.util.Set;

/**
 * Created by sblackmon on 10/20/14.
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@ElasticsearchIntegrationTest.ClusterScope(scope= ElasticsearchIntegrationTest.Scope.TEST, numNodes=1)
public class TestElasticsearchPersistWriterParentChildIT extends ElasticsearchIntegrationTest {

    protected String TEST_INDEX = "TestElasticsearchPersistWriter".toLowerCase();

    private final static Logger LOGGER = LoggerFactory.getLogger(TestElasticsearchPersistWriterParentChildIT.class);

    private static ObjectMapper MAPPER = StreamsJacksonMapper.getInstance();

    protected ElasticsearchWriterConfiguration testConfiguration;

    Set<Class<? extends ActivityObject>> objectTypes;

    List<String> files;

    @Before
    public void prepareTest() throws Exception {

        testConfiguration = new ElasticsearchWriterConfiguration();
        testConfiguration.setHosts(Lists.newArrayList("localhost"));
        testConfiguration.setClusterName(cluster().getClusterName());
        testConfiguration.setIndex("activity");
        testConfiguration.setBatchSize(5l);

        PutIndexTemplateRequestBuilder putTemplateRequestBuilder = client().admin().indices().preparePutTemplate("mappings");
        URL templateURL = TestElasticsearchPersistWriterParentChildIT.class.getResource("/ActivityChildObjectParent.json");
        ObjectNode template = MAPPER.readValue(templateURL, ObjectNode.class);
        String templateSource = MAPPER.writeValueAsString(template);
        putTemplateRequestBuilder.setSource(templateSource);

        client().admin().indices().putTemplate(putTemplateRequestBuilder.request()).actionGet();

        Reflections reflections = new Reflections(new ConfigurationBuilder()
                .setUrls(ClasspathHelper.forPackage("org.apache.streams.pojo.json"))
                .setScanners(new SubTypesScanner()));
        objectTypes = reflections.getSubTypesOf(ActivityObject.class);

        InputStream testActivityFolderStream = TestElasticsearchPersistWriterParentChildIT.class.getClassLoader()
                .getResourceAsStream("activities");
        files = IOUtils.readLines(testActivityFolderStream, Charsets.UTF_8);

    }

    @Test
    public void testPersist() throws Exception {
        testPersistWriter();
        testPersistUpdater();
    }

    void testPersistWriter() throws Exception {

        assert(!indexExists(TEST_INDEX));

        testConfiguration.setIndex("activity");
        testConfiguration.setBatchSize(5l);

        ElasticsearchPersistWriter testPersistWriter = new ElasticsearchPersistWriter(testConfiguration);
        testPersistWriter.prepare(null);

        for( Class objectType : objectTypes ) {
            Object object = objectType.newInstance();
            ActivityObject activityObject = MAPPER.convertValue(object, ActivityObject.class);
            StreamsDatum datum = new StreamsDatum(activityObject, activityObject.getObjectType());
            datum.getMetadata().put("type", "object");
            testPersistWriter.write( datum );
        }

        for( String file : files) {
            LOGGER.info("File: " + file );
            InputStream testActivityFileStream = TestElasticsearchPersistWriterParentChildIT.class.getClassLoader()
                    .getResourceAsStream("activities/" + file);
            Activity activity = MAPPER.readValue(testActivityFileStream, Activity.class);
            StreamsDatum datum = new StreamsDatum(activity, activity.getVerb());
            if( !Strings.isNullOrEmpty(activity.getObject().getObjectType())) {
                datum.getMetadata().put("parent", activity.getObject().getObjectType());
                datum.getMetadata().put("type", "activity");
                testPersistWriter.write(datum);
                LOGGER.info("Wrote: " + activity.getVerb());
            }
        }

        testPersistWriter.cleanUp();

        flushAndRefresh();

        long parent_count = client().count(client().prepareCount().setTypes("object").request()).actionGet().getCount();

        assertEquals(41, parent_count);

        long child_count = client().count(client().prepareCount().setTypes("activity").request()).actionGet().getCount();

        assertEquals(84, child_count);

    }

    void testPersistUpdater() throws Exception {

        ElasticsearchPersistUpdater testPersistUpdater = new ElasticsearchPersistUpdater(testConfiguration);
        testPersistUpdater.prepare(null);

        for( String file : files) {
            LOGGER.info("File: " + file );
            InputStream testActivityFileStream = TestElasticsearchPersistWriterParentChildIT.class.getClassLoader()
                    .getResourceAsStream("activities/" + file);
            Activity activity = MAPPER.readValue(testActivityFileStream, Activity.class);
            activity.setAdditionalProperty("updated", Boolean.TRUE);
            StreamsDatum datum = new StreamsDatum(activity, activity.getVerb());
            if( !Strings.isNullOrEmpty(activity.getObject().getObjectType())) {
                datum.getMetadata().put("parent", activity.getObject().getObjectType());
                datum.getMetadata().put("type", "activity");
                testPersistUpdater.write(datum);
                LOGGER.info("Updated: " + activity.getVerb() );
            }
        }

        testPersistUpdater.cleanUp();

        flushAndRefresh();

        long child_count = client().count(client().prepareCount().setQuery(QueryBuilders.termQuery("updated", "true")).setTypes("activity").request()).actionGet().getCount();

        assertEquals(84, child_count);

    }
}
