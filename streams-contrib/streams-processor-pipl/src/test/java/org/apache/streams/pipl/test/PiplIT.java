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

package org.apache.streams.pipl.test;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.apache.streams.config.ComponentConfigurator;
import org.apache.streams.config.StreamsConfigurator;
import org.apache.streams.pipl.Pipl;
import org.apache.streams.pipl.Search;
import org.apache.streams.pipl.api.BasicSearchRequest;
import org.apache.streams.pipl.api.SearchResponse;
import org.apache.streams.pipl.config.PiplConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.File;

import static java.util.Objects.nonNull;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.testng.Assert.assertEquals;

/**
 * Integration Tests for all implemented pipl.com endpoints.
 */
public class PiplIT {

    private static final Logger LOGGER = LoggerFactory.getLogger(PiplIT.class);

    private static String configfile = "target/test-classes/PiplIT/PiplIT.conf";

    private static PiplConfiguration config;

    private static Config testsconfig;

    @BeforeClass(alwaysRun = true)
    public void setup() throws Exception {
        File conf = new File(configfile);
        Assert.assertTrue (conf.exists());
        Assert.assertTrue (conf.canRead());
        Assert.assertTrue (conf.isFile());
        StreamsConfigurator.addConfig(ConfigFactory.parseFileAnySyntax(conf));
        config = new ComponentConfigurator<>(PiplConfiguration.class).detectConfiguration();
        testsconfig = StreamsConfigurator.getConfig().getConfig("org.apache.streams.pipl.test.PiplIT");
    }

    @Test
    public void testBasicSearchByEmail() throws Exception {
        Search search = Pipl.getInstance(config);
        String email = testsconfig.getString("testBasicSearchByEmail.email");
        BasicSearchRequest req = new BasicSearchRequest()
            .withEmail(email)
            .withTopMatch(true);
        SearchResponse response = search.basicSearch(req);
        nonNull(response);
        nonNull(response.getHttpStatusCode());
        assertEquals(response.getHttpStatusCode(), new Long(200));
        nonNull(response.getPerson());
        assertThat("response contains at least one email address", response.getPerson().getEmails().size() >= 1);
    }

    @Test
    public void testFullPersonSearchByEmail() throws Exception {
        Search search = Pipl.getInstance(config);
        String email = testsconfig.getString("testFullPersonSearchByEmail.email");
        BasicSearchRequest req = new BasicSearchRequest()
                .withEmail(email)
                .withTopMatch(true);
        SearchResponse response = search.basicSearch(req);
        nonNull(response);
        nonNull(response.getHttpStatusCode());
        assertEquals(response.getHttpStatusCode(), new Long(200));
        nonNull(response.getPerson());
        assertThat("response contains at least one email address", response.getPerson().getEmails().size() >= 1);
    }

    @Test
    public void testHandlesMissCorrectly() throws Exception {
        Search search = Pipl.getInstance(config);
        String email = testsconfig.getString("testHandlesMissCorrectly.email");
        BasicSearchRequest req = new BasicSearchRequest()
                .withEmail(email)
                .withTopMatch(true);
        SearchResponse response = search.basicSearch(req);
        nonNull(response);
        nonNull(response.getHttpStatusCode());
        assertEquals(response.getHttpStatusCode(), new Long(404));
        nonNull(response.getError());
        assertEquals(response.getError(), "not_found");
    }

}