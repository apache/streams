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

package org.apache.streams.jackson;

import org.apache.streams.pojo.json.MemoryUsageBroadcast;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import org.slf4j.Logger;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.util.Arrays;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanInfo;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.management.openmbean.CompositeDataSupport;

public class MemoryUsageDeserializer extends JsonDeserializer<MemoryUsageBroadcast> {

  private static final Logger LOGGER = org.slf4j.LoggerFactory.getLogger(MemoryUsageDeserializer.class);

  public MemoryUsageDeserializer() {

  }

  @Override
  public MemoryUsageBroadcast deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JsonProcessingException {
    try {
      MBeanServer server = ManagementFactory.getPlatformMBeanServer();

      MemoryUsageBroadcast memoryUsageBroadcast = new MemoryUsageBroadcast();
      JsonNode attributes = jsonParser.getCodec().readTree(jsonParser);

      ObjectName name = new ObjectName(attributes.get("canonicalName").asText());
      MBeanInfo info = server.getMBeanInfo(name);
      memoryUsageBroadcast.setName(name.toString());

      for (MBeanAttributeInfo attribute : Arrays.asList(info.getAttributes())) {
        switch (attribute.getName()) {
          case "Verbose":
            memoryUsageBroadcast.setVerbose((boolean) server.getAttribute(name, attribute.getName()));
            break;
          case "ObjectPendingFinalizationCount":
            memoryUsageBroadcast.setObjectPendingFinalizationCount(Long.parseLong(server.getAttribute(name, attribute.getName()).toString()));
            break;
          case "HeapMemoryUsage":
            memoryUsageBroadcast.setHeapMemoryUsage((Long) ((CompositeDataSupport)server.getAttribute(name, attribute.getName())).get("used"));
            break;
          case "NonHeapMemoryUsage":
            memoryUsageBroadcast.setNonHeapMemoryUsage((Long) ((CompositeDataSupport)server.getAttribute(name, attribute.getName())).get("used"));
            break;
          default:
            break;
        }
      }

      return memoryUsageBroadcast;
    } catch (Exception ex) {
      LOGGER.error("Exception trying to deserialize MemoryUsageDeserializer object: {}", ex);
      return null;
    }
  }
}
