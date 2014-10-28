/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
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

package com.google.gplus;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.node.IntNode;
import com.google.api.client.util.Lists;
import com.google.api.services.plus.model.Person;
import com.google.gplus.serializer.util.GooglePlusActivityUtil;
import org.apache.commons.lang.StringUtils;
import org.apache.streams.jackson.StreamsJacksonMapper;
import org.apache.streams.pojo.json.Activity;
import org.apache.streams.pojo.json.Actor;
import org.apache.streams.pojo.json.Provider;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class GooglePlusPersonSerDeTest {
    private final static Logger LOGGER = LoggerFactory.getLogger(GooglePlusPersonSerDeTest.class);

    @Test
    public void TestPersonObjects() {
        ObjectMapper objectMapper = new StreamsJacksonMapper();
        SimpleModule simpleModule = new SimpleModule();
        simpleModule.addDeserializer(Person.class, new GPlusPersonDeserializer());
        objectMapper.registerModule(simpleModule);

        InputStream is = GooglePlusPersonSerDeTest.class.getResourceAsStream("/google_plus_person_jsons.txt");
        InputStreamReader isr = new InputStreamReader(is);
        BufferedReader br = new BufferedReader(isr);

        GooglePlusActivityUtil googlePlusActivityUtil = new GooglePlusActivityUtil();

        try {
            while (br.ready()) {
                String line = br.readLine();
                if (!StringUtils.isEmpty(line)) {
                    LOGGER.info("raw: {}", line);
                    objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
                    Activity activity = new Activity();

                    Person person = objectMapper.readValue(line, Person.class);

                    googlePlusActivityUtil.updateActivity(person, activity);
                    LOGGER.info("activity: {}", activity);

                    assertNotNull(activity);
                    assert(activity.getId().contains("id:googleplus:update"));
                    assertEquals(activity.getVerb(), "update");

                    Provider provider = activity.getProvider();
                    assertEquals(provider.getId(), "id:providers:googleplus");
                    assertEquals(provider.getDisplayName(), "GooglePlus");

                    Actor actor = activity.getActor();
                    assertNotNull(actor.getImage());
                    assert(actor.getId().contains("id:googleplus:"));
                    assertNotNull(actor.getUrl());

                }
            }
        } catch (Exception e) {
            LOGGER.error("Exception while testing serializability: {}", e);
        }
    }
}

class GPlusPersonDeserializer extends JsonDeserializer<Person> {
    @Override
    public Person deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JsonProcessingException {
        ObjectMapper m = new StreamsJacksonMapper();

        JsonNode node = jsonParser.getCodec().readTree(jsonParser);
        Person person = new Person();

        person.setCircledByCount((Integer)((IntNode)node.get("circledByCount")).numberValue());
        person.setDisplayName(node.get("displayName").asText());
        person.setEtag(node.get("etag").asText());
        person.setGender(node.get("gender").asText());
        person.setId(node.get("id").asText());

        Person.Image image = new Person.Image();
        JsonNode imageNode = node.get("image");
        image.setIsDefault(imageNode.get("isDefault").asBoolean());
        image.setUrl(imageNode.get("url").asText());
        person.setImage(image);

        person.setIsPlusUser(node.get("isPlusUser").asBoolean());
        person.setKind(node.get("kind").asText());

        JsonNode nameNode = node.get("name");
        Person.Name name = m.readValue(m.writeValueAsString(nameNode), Person.Name.class);
        person.setName(name);

        person.setObjectType(node.get("objectType").asText());

        List<Person.Organizations> organizations = Lists.newArrayList();
        for(JsonNode orgNode : node.get("organizations")) {
            Person.Organizations org = m.readValue(m.writeValueAsString(orgNode), Person.Organizations.class);
            organizations.add(org);
        }
        person.setOrganizations(organizations);

        person.setUrl(node.get("url").asText());
        person.setVerified(node.get("verified").asBoolean());

        return person;
    }
}