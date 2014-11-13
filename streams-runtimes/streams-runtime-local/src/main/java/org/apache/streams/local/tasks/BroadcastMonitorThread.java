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
package org.apache.streams.local.tasks;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.google.common.collect.Lists;
import org.apache.streams.jackson.*;
import org.apache.streams.pojo.json.*;
import org.slf4j.Logger;

import javax.management.*;
import java.lang.management.ManagementFactory;
import java.util.List;
import java.util.Set;

public class BroadcastMonitorThread extends NotificationBroadcasterSupport implements Runnable {
    private static final Logger LOGGER = org.slf4j.LoggerFactory.getLogger(BroadcastMonitorThread.class);
    private static MBeanServer server;
    private long DEFAULT_WAIT_TIME = 30000;
    private ObjectMapper objectMapper;

    public BroadcastMonitorThread() {
        server = ManagementFactory.getPlatformMBeanServer();

        objectMapper = new StreamsJacksonMapper();
        SimpleModule simpleModule = new SimpleModule();

        simpleModule.addDeserializer(MemoryUsageBroadcast.class, new MemoryUsageDeserializer());
        simpleModule.addDeserializer(ThroughputQueueBroadcast.class, new ThroughputQueueDeserializer());
        simpleModule.addDeserializer(StreamsTaskCounterBroadcast.class, new StreamsTaskCounterDeserializer());
        simpleModule.addDeserializer(DatumStatusCounterBroadcast.class, new DatumStatusCounterDeserializer());

        objectMapper.registerModule(simpleModule);
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    @Override
    public void run() {
        while(true) {
            try {
                List<String> messages = Lists.newArrayList();
                Set<ObjectName> beans = server.queryNames(null, null);

                for(ObjectName name : beans) {
                    String item = objectMapper.writeValueAsString(name);
                    Broadcast broadcast = null;

                    if(name.getKeyPropertyList().get("type") != null) {
                        if (name.getKeyPropertyList().get("type").equals("ThroughputQueue")) {
                            broadcast = objectMapper.readValue(item, ThroughputQueueBroadcast.class);
                        } else if (name.getKeyPropertyList().get("type").equals("StreamsTaskCounter")) {
                            broadcast = objectMapper.readValue(item, StreamsTaskCounterBroadcast.class);
                        } else if (name.getKeyPropertyList().get("type").equals("DatumStatusCounter")) {
                            broadcast = objectMapper.readValue(item, DatumStatusCounterBroadcast.class);
                        } else if (name.getKeyPropertyList().get("type").equals("Memory")) {
                            broadcast = objectMapper.readValue(item, MemoryUsageBroadcast.class);
                        }

                        if(broadcast != null) {
                            messages.add(objectMapper.writeValueAsString(broadcast));
                        }
                    }
                }

                Thread.sleep(DEFAULT_WAIT_TIME);
            } catch (InterruptedException e) {
                LOGGER.error("Interrupted!: {}", e);
            } catch (Exception e) {
                LOGGER.error("Exception: {}", e);
            }
        }
    }
}