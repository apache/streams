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

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.apache.commons.lang.StringUtils;
import org.apache.streams.pojo.json.*;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

import static org.junit.Assert.assertNotNull;

public class MemoryUsageDeserializerTest {

    private final static Logger LOGGER = LoggerFactory.getLogger(MemoryUsageDeserializerTest.class);
    private ObjectMapper objectMapper;

    @Before
    public void setup() {
        objectMapper = StreamsJacksonMapper.getInstance();
        SimpleModule simpleModule = new SimpleModule();
        simpleModule.addDeserializer(MemoryUsageBroadcast.class, new MemoryUsageDeserializer());
        objectMapper.registerModule(simpleModule);
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    @Test
    public void serDeTest() {
        InputStream is = MemoryUsageDeserializerTest.class.getResourceAsStream("/MemoryUsageObjects.json");
        InputStreamReader isr = new InputStreamReader(is);
        BufferedReader br = new BufferedReader(isr);

        try {
            while (br.ready()) {
                String line = br.readLine();
                if (!StringUtils.isEmpty(line)) {
                    LOGGER.info("raw: {}", line);
                    MemoryUsageBroadcast broadcast = objectMapper.readValue(line, MemoryUsageBroadcast.class);

                    LOGGER.info("activity: {}", broadcast);

                    assertNotNull(broadcast);
                    assertNotNull(broadcast.getVerbose());
                    assertNotNull(broadcast.getObjectPendingFinalizationCount());
                    assertNotNull(broadcast.getHeapMemoryUsage());
                    assertNotNull(broadcast.getNonHeapMemoryUsage());
                    assertNotNull(broadcast.getName());
                }
            }
        } catch (Exception e) {
            LOGGER.error("Exception while testing serializability: {}", e);
        }
    }
}
