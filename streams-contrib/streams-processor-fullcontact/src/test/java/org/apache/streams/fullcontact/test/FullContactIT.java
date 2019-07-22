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

package org.apache.streams.fullcontact.test;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import com.typesafe.config.ConfigRenderOptions;
import org.apache.commons.lang.StringUtils;
import org.apache.juneau.json.JsonParser;
import org.apache.streams.config.ComponentConfigurator;
import org.apache.streams.config.StreamsConfigurator;
import org.apache.streams.fullcontact.CompanyEnrichment;
import org.apache.streams.fullcontact.FullContact;
import org.apache.streams.fullcontact.PersonEnrichment;
import org.apache.streams.fullcontact.api.*;
import org.apache.streams.fullcontact.config.FullContactConfiguration;
import org.apache.streams.fullcontact.pojo.CompanySummary;
import org.apache.streams.fullcontact.pojo.PersonSummary;
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
 * Integration Tests for all implemented fullcontact.com endpoints.
 */
public class FullContactIT {

    private static final Logger LOGGER = LoggerFactory.getLogger(FullContactIT.class);

    private static String configfile = "target/test-classes/FullContactIT/FullContactIT.conf";

    private static FullContactConfiguration config;

    @BeforeClass(alwaysRun = true)
    public void setup() throws Exception {
        File conf = new File(configfile);
        Assert.assertTrue (conf.exists());
        Assert.assertTrue (conf.canRead());
        Assert.assertTrue (conf.isFile());
        StreamsConfigurator.addConfig(ConfigFactory.parseFileAnySyntax(conf));
        config = new ComponentConfigurator<>(FullContactConfiguration.class).detectConfiguration();
    }

    @Test
    public void testEnrichCompanyByDomain() throws Exception {
        CompanyEnrichment companyEnrichment = FullContact.getInstance(config);
        String domain = StreamsConfigurator.getConfig().getString("org.apache.streams.fullcontact.test.FullContactIT.testEnrichCompanyByDomain.domain");
        EnrichCompanyRequest req = new EnrichCompanyRequest()
                .withDomain(domain);
        CompanySummary response = companyEnrichment.enrichCompany(req);
        nonNull(response);
        nonNull(response.getName());
        assertThat("response contains a non-empty name", StringUtils.isNotBlank(response.getName()));
    }

    @Test
    public void testEnrichPersonByEmail() throws Exception {
        PersonEnrichment personEnrichment = FullContact.getInstance(config);
        String email = StreamsConfigurator.getConfig().getString("org.apache.streams.fullcontact.test.FullContactIT.testEnrichPersonByEmail.email");
        EnrichPersonRequest req = new EnrichPersonRequest()
            .withEmail(email);
        PersonSummary response = personEnrichment.enrichPerson(req);
        nonNull(response);
        nonNull(response.getFullName());
        assertThat("response contains a non-empty fullName", StringUtils.isNotBlank(response.getFullName()));
    }

    @Test
    public void testEnrichPersonByEmailHash() throws Exception {
        PersonEnrichment personEnrichment = FullContact.getInstance(config);
        String email = StreamsConfigurator.getConfig().getString("org.apache.streams.fullcontact.test.FullContactIT.testEnrichPersonByEmailHash.emailHash");
        EnrichPersonRequest req = new EnrichPersonRequest()
                .withEmail(email);
        PersonSummary response = personEnrichment.enrichPerson(req);
        nonNull(response);
        nonNull(response.getFullName());
        assertThat("response contains a non-empty fullName", StringUtils.isNotBlank(response.getFullName()));
    }

    @Test
    public void testEnrichPersonByEmails() throws Exception {
        PersonEnrichment personEnrichment = FullContact.getInstance(config);
        List<String> emails = StreamsConfigurator.getConfig().getStringList("org.apache.streams.fullcontact.test.FullContactIT.testEnrichPersonByEmails.emails");
        EnrichPersonRequest req = new EnrichPersonRequest()
                .withEmails(emails);
        PersonSummary response = personEnrichment.enrichPerson(req);
        nonNull(response);
        nonNull(response.getFullName());
        assertThat("response contains a non-empty fullName", StringUtils.isNotBlank(response.getFullName()));
    }

    @Test
    public void testEnrichPersonByTwitterUserid() throws Exception {
        PersonEnrichment personEnrichment = FullContact.getInstance(config);
        String userid = StreamsConfigurator.getConfig().getString("org.apache.streams.fullcontact.test.FullContactIT.testEnrichPersonByTwitterUserid.userid");
        EnrichPersonRequest req = new EnrichPersonRequest()
                .withProfiles(
                        Lists.newArrayList(
                                new ProfileQuery()
                                        .withService("twitter")
                                        .withUserid(userid)
                        )
                );
        PersonSummary response = personEnrichment.enrichPerson(req);
        nonNull(response);
        nonNull(response.getFullName());
        assertThat("response contains a non-empty fullName", StringUtils.isNotBlank(response.getFullName()));
    }

    @Test
    public void testEnrichPersonByTwitterUsername() throws Exception {
        PersonEnrichment personEnrichment = FullContact.getInstance(config);
        String username = StreamsConfigurator.getConfig().getString("org.apache.streams.fullcontact.test.FullContactIT.testEnrichPersonByTwitterUsername.username");
        EnrichPersonRequest req = new EnrichPersonRequest()
                .withProfiles(
                        Lists.newArrayList(
                                new ProfileQuery()
                                        .withService("twitter")
                                        .withUsername(username)
                        )
                );
        PersonSummary response = personEnrichment.enrichPerson(req);
        nonNull(response);
        nonNull(response.getFullName());
        assertThat("response contains a non-empty fullName", StringUtils.isNotBlank(response.getFullName()));
    }

    @Test
    public void testEnrichPersonByGithubUrl() throws Exception {
        PersonEnrichment personEnrichment = FullContact.getInstance(config);
        String url = StreamsConfigurator.getConfig().getString("org.apache.streams.fullcontact.test.FullContactIT.testEnrichPersonByGithubUrl.url");
        EnrichPersonRequest req = new EnrichPersonRequest()
                .withProfiles(
                        Lists.newArrayList(
                                new ProfileQuery()
                                .withService("github")
                                .withUrl(url)
                        )
                );
        PersonSummary response = personEnrichment.enrichPerson(req);
        nonNull(response);
        nonNull(response.getFullName());
        assertThat("response contains a non-empty fullName", StringUtils.isNotBlank(response.getFullName()));
    }

    @Test
    public void testEnrichPersonByLinkedinUsername() throws Exception {
        PersonEnrichment personEnrichment = FullContact.getInstance(config);
        String username = StreamsConfigurator.getConfig().getString("org.apache.streams.fullcontact.test.FullContactIT.testEnrichPersonByLinkedinUsername.username");
        EnrichPersonRequest req = new EnrichPersonRequest()
                .withProfiles(
                        Lists.newArrayList(
                                new ProfileQuery()
                                        .withService("linkedin")
                                        .withUsername(username)
                        )
                );
        PersonSummary response = personEnrichment.enrichPerson(req);
        nonNull(response);
        nonNull(response.getFullName());
        assertThat("response contains a non-empty fullName", StringUtils.isNotBlank(response.getFullName()));
    }

    @Test
    public void testEnrichPersonByPhone() throws Exception {
        PersonEnrichment personEnrichment = FullContact.getInstance(config);
        String phone = StreamsConfigurator.getConfig().getString("org.apache.streams.fullcontact.test.FullContactIT.testEnrichPersonByPhone.phone");
        EnrichPersonRequest req = new EnrichPersonRequest()
                .withPhone(phone);
        PersonSummary response = personEnrichment.enrichPerson(req);
        nonNull(response);
        nonNull(response.getFullName());
        assertThat("response contains a non-empty fullName", StringUtils.isNotBlank(response.getFullName()));
    }

    @Test
    public void testEnrichPersonByPhones() throws Exception {
        PersonEnrichment personEnrichment = FullContact.getInstance(config);
        List<String> phones = StreamsConfigurator.getConfig().getStringList("org.apache.streams.fullcontact.test.FullContactIT.testEnrichPersonByPhones.phones");
        EnrichPersonRequest req = new EnrichPersonRequest()
                .withPhones(phones);
        PersonSummary response = personEnrichment.enrichPerson(req);
        nonNull(response);
        nonNull(response.getFullName());
        assertThat("response contains a non-empty fullName", StringUtils.isNotBlank(response.getFullName()));
    }

    @Test
    public void testEnrichPersonByNameAndLocation() throws Exception {
        PersonEnrichment personEnrichment = FullContact.getInstance(config);
        Config location = StreamsConfigurator.getConfig().getConfig("org.apache.streams.fullcontact.test.FullContactIT.testEnrichPersonByNameAndLocation.location");
        LocationQuery locationQuery = JsonParser.DEFAULT.parse(location.root().render(ConfigRenderOptions.concise()), LocationQuery.class);
        Config name = StreamsConfigurator.getConfig().getConfig("org.apache.streams.fullcontact.test.FullContactIT.testEnrichPersonByNameAndLocation.name");
        NameQuery nameQuery = JsonParser.DEFAULT.parse(name.root().render(ConfigRenderOptions.concise()), NameQuery.class);
        EnrichPersonRequest req = new EnrichPersonRequest()
                .withLocation(locationQuery)
                .withName(nameQuery);
        PersonSummary response = personEnrichment.enrichPerson(req);
        nonNull(response);
        nonNull(response.getFullName());
        assertThat("response contains a non-empty fullName", StringUtils.isNotBlank(response.getFullName()));
    }

}