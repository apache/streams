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

package org.apache.streams.peopledatalabs.test;

import com.typesafe.config.ConfigFactory;
import org.apache.streams.config.ComponentConfigurator;
import org.apache.streams.config.StreamsConfigurator;
import org.apache.streams.peopledatalabs.PeopleDataLabs;
import org.apache.streams.peopledatalabs.PersonEnrichment;
import org.apache.streams.peopledatalabs.api.*;
import org.apache.streams.peopledatalabs.config.PeopleDataLabsConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.testng.collections.Lists;

import java.io.File;
import java.util.List;

import static java.util.Objects.nonNull;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.testng.Assert.assertEquals;

/**
 * Integration Tests for all implemented peopledatalabs.com endpoints.
 */
public class PeopleDataLabsIT {

    private static final Logger LOGGER = LoggerFactory.getLogger(PeopleDataLabsIT.class);

    private static String configfile = "target/test-classes/PeopleDataLabsIT.conf";

    private static PeopleDataLabsConfiguration config;

    @BeforeClass(alwaysRun = true)
    public void setup() throws Exception {
        File conf = new File(configfile);
        Assert.assertTrue (conf.exists());
        Assert.assertTrue (conf.canRead());
        Assert.assertTrue (conf.isFile());
        StreamsConfigurator.addConfig(ConfigFactory.parseFileAnySyntax(conf));
        config = new ComponentConfigurator<>(PeopleDataLabsConfiguration.class).detectConfiguration();
    }

    @Test
    public void testSearchByEmail() throws Exception {
        PersonEnrichment personEnrichment = PeopleDataLabs.getInstance(config);
        EnrichPersonRequest req = new EnrichPersonRequest()
            .withEmail("sean@peopledatalabs.com");
        EnrichPersonResponse response = personEnrichment.enrichPerson(req);
        nonNull(response);
        nonNull(response.getStatus());
        nonNull(response.getMetadata());
        nonNull(response.getData());
        assertEquals(response.getStatus(), new Long(200));
        assertThat("response contains at least one email address", response.getData().getEmails().size() >= 1);
    }

    @Test
    public void testSearchByLinkedinUrl() throws Exception {
        PersonEnrichment personEnrichment = PeopleDataLabs.getInstance(config);
        EnrichPersonRequest req = new EnrichPersonRequest()
            .withProfile("linkedin.com/in/seanthorne");
        EnrichPersonResponse response = personEnrichment.enrichPerson(req);
        nonNull(response);
        nonNull(response.getStatus());
        nonNull(response.getMetadata());
        nonNull(response.getData());
        assertEquals(response.getStatus(), new Long(200));
        assertThat("response contains at least one profile", response.getData().getProfiles().size() >= 1);
    }

    @Test
    public void testSearchByNameLocationCompany() throws Exception {
        PersonEnrichment personEnrichment = PeopleDataLabs.getInstance(config);
        EnrichPersonRequest req = new EnrichPersonRequest()
            .withName("Sean Thorne")
            .withLocation("San Francisco")
            .withCompany("People Data Labs");
        EnrichPersonResponse response = personEnrichment.enrichPerson(req);
        nonNull(response);
        nonNull(response.getStatus());
        nonNull(response.getMetadata());
        nonNull(response.getData());
        assertEquals(response.getStatus(), new Long(200));
        assertThat("response contains at least one location", response.getData().getLocations().size() >= 1);
    }

    @Test
    public void testBulkEnrichment() throws Exception {
        PersonEnrichment personEnrichment = PeopleDataLabs.getInstance(config);
        BulkEnrichPersonRequestItem item1 = new BulkEnrichPersonRequestItem()
                .withParams(new Params().withEmail(Lists.newArrayList("sblackmon@apache.org")));
        BulkEnrichPersonRequestItem item2 = new BulkEnrichPersonRequestItem()
                .withParams(new Params().withEmail(Lists.newArrayList("smarthi@apache.org")));
        BulkEnrichPersonRequestItem item3 = new BulkEnrichPersonRequestItem()
                .withParams(new Params().withEmail(Lists.newArrayList("jfrazee@apache.org")));
        List<BulkEnrichPersonRequestItem> reqList = Lists.newArrayList(item1, item2, item3);
        BulkEnrichPersonRequest bulkRequest = new BulkEnrichPersonRequest().withRequests(reqList);
        List<EnrichPersonResponse> response = personEnrichment.bulkEnrichPerson(bulkRequest);
        nonNull(response);
        assertThat("response contains three response items", response.size() == 3);
    }
}