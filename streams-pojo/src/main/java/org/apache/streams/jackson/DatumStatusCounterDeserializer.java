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

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import org.apache.streams.pojo.json.DatumStatusCounterBroadcast;
import org.slf4j.Logger;

import javax.management.MBeanAttributeInfo;
import javax.management.MBeanInfo;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.util.Arrays;

public class DatumStatusCounterDeserializer extends JsonDeserializer<DatumStatusCounterBroadcast> {
    private static final Logger LOGGER = org.slf4j.LoggerFactory.getLogger(DatumStatusCounterDeserializer.class);

    public DatumStatusCounterDeserializer() {

    }

    @Override
    public DatumStatusCounterBroadcast deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JsonProcessingException {
        try {
            MBeanServer server = ManagementFactory.getPlatformMBeanServer();

            DatumStatusCounterBroadcast datumStatusCounterBroadcast = new DatumStatusCounterBroadcast();
            JsonNode attributes = jsonParser.getCodec().readTree(jsonParser);

            ObjectName name = new ObjectName(attributes.get("canonicalName").asText());
            MBeanInfo info = server.getMBeanInfo(name);
            datumStatusCounterBroadcast.setName(name.toString());

            for (MBeanAttributeInfo attribute : Arrays.asList(info.getAttributes())) {
                try {
                    switch(attribute.getName()) {
                        case "Failed":
                            datumStatusCounterBroadcast.setFailed((boolean) server.getAttribute(name, attribute.getName()));
                            break;
                        case "Passed":
                            datumStatusCounterBroadcast.setPassed((boolean) server.getAttribute(name, attribute.getName()));
                            break;
                    }
                } catch (Exception e) {
                    LOGGER.error("Exception trying to deserialize DatumStatusCounterBroadcast object: {}", e);
                }
            }

            return datumStatusCounterBroadcast;
        } catch (Exception e) {
            LOGGER.error("Exception trying to deserialize DatumStatusCounterBroadcast object: {}", e);
            return null;
        }
    }
}
