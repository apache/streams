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

package org.apache.streams.gplus.serializer.util;

import org.apache.streams.jackson.StreamsJacksonMapper;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.services.plus.model.Person;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Custom deserializer for GooglePlus' Person model.
 */
public class GPlusPersonDeserializer extends JsonDeserializer<Person> {

  private static final Logger LOGGER = LoggerFactory.getLogger(GPlusPersonDeserializer.class);

  /**
   * Because the GooglePlus Person object contains complex objects within its hierarchy, we have to use
   * a custom deserializer
   *
   * @param jsonParser jsonParser
   * @param deserializationContext deserializationContext
   * @return The deserialized {@link com.google.api.services.plus.model.Person} object
   * @throws IOException IOException
   * @throws JsonProcessingException JsonProcessingException
   */
  @Override
  public Person deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
    ObjectMapper mapper = StreamsJacksonMapper.getInstance();

    JsonNode node = jsonParser.getCodec().readTree(jsonParser);
    Person person = new Person();
    try {
      person.setId(node.get("id").asText());
      person.setCircledByCount((Integer) (node.get("circledByCount")).numberValue());
      person.setDisplayName(node.get("displayName").asText());
      if( node.has("etag")) {
        person.setEtag(node.get("etag").asText());
      }
      if( node.has("gender")) {
        person.setGender(node.get("gender").asText());
      }

      Person.Image image = new Person.Image();
      if( node.has("image") ) {
        JsonNode imageNode = node.get("image");
        image.setIsDefault(imageNode.get("isDefault").asBoolean());
        image.setUrl(imageNode.get("url").asText());
        person.setImage(image);
      }

      person.setIsPlusUser(node.get("isPlusUser").asBoolean());
      person.setKind(node.get("kind").asText());

      JsonNode nameNode = node.get("name");
      Person.Name name = mapper.readValue(mapper.writeValueAsString(nameNode), Person.Name.class);
      person.setName(name);

      person.setObjectType(node.get("objectType").asText());

      List<Person.Organizations> organizations = new ArrayList<>();
      if( node.has("organizations")) {
        for (JsonNode orgNode : node.get("organizations")) {
          Person.Organizations org = mapper.readValue(mapper.writeValueAsString(orgNode), Person.Organizations.class);
          organizations.add(org);
        }
        person.setOrganizations(organizations);
      }

      person.setUrl(node.get("url").asText());
      person.setVerified(node.get("verified").asBoolean());

      List<Person.Emails> emails = new ArrayList<>();

      if ( node.has("emails")) {
        for (JsonNode emailNode : node.get("emails")) {
          Person.Emails email = mapper.readValue(mapper.writeValueAsString(emailNode), Person.Emails.class);
          emails.add(email);
        }
      }

      if ( node.has("tagline")) {
        person.setTagline(node.get("tagline").asText());
      }
      if ( node.has("aboutMe")) {
        person.setAboutMe(node.get("aboutMe").asText());
      }
    } catch (Exception ex) {
      LOGGER.error("Exception while trying to deserialize a Person object: {}", ex);
    }

    return person;
  }
}