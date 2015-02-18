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
package org.apache.streams.monitoring.tasks;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.google.common.collect.Lists;
import org.apache.streams.jackson.*;
import org.apache.streams.monitoring.persist.MessagePersister;
import org.apache.streams.monitoring.persist.impl.BroadcastMessagePersister;
import org.apache.streams.monitoring.persist.impl.LogstashUdpMessagePersister;
import org.apache.streams.monitoring.persist.impl.SLF4JMessagePersister;
import org.apache.streams.pojo.json.*;
import org.slf4j.Logger;

import javax.management.*;
import java.lang.management.ManagementFactory;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * This thread runs inside of a Streams runtime and periodically persists information
 * from relevant JMX beans
 */
public class BroadcastMonitorThread extends NotificationBroadcasterSupport implements Runnable {
    private static final Logger LOGGER = org.slf4j.LoggerFactory.getLogger(BroadcastMonitorThread.class);
    private static MBeanServer server;

    private long DEFAULT_WAIT_TIME = 30000;
    private long waitTime;
    private ObjectMapper objectMapper;
    private Map<String, Object> streamConfig;
    private URI broadcastURI = null;
    private MessagePersister messagePersister;
    private volatile boolean keepRunning;

    public BroadcastMonitorThread(Map<String, Object> streamConfig) {
        keepRunning = true;
        this.streamConfig = streamConfig;

        LOGGER.info("BroadcastMonitorThread starting" + streamConfig);

        server = ManagementFactory.getPlatformMBeanServer();

        setBroadcastURI();
        setWaitTime();

        if( broadcastURI != null ) {
            if (broadcastURI.getScheme().equals("http")) {
                messagePersister = new BroadcastMessagePersister(broadcastURI.toString());
            } else if (broadcastURI.getScheme().equals("udp")) {
                messagePersister = new LogstashUdpMessagePersister(broadcastURI.toString());
            } else {
                LOGGER.error("You need to specify a broadcast URI with either a HTTP or UDP protocol defined.");
                throw new RuntimeException();
            }
        } else {
            messagePersister = new SLF4JMessagePersister();
        }

        initializeObjectMapper();

        LOGGER.info("BroadcastMonitorThread started");
    }

    /**
     * Initialize our object mapper with all of our bean's custom deserializers
     * This way we can convert them to and from Strings dictated by our
     * POJOs which are generated from JSON schemas
     */
    private void initializeObjectMapper() {
        objectMapper = StreamsJacksonMapper.getInstance();
        SimpleModule simpleModule = new SimpleModule();

        simpleModule.addDeserializer(MemoryUsageBroadcast.class, new MemoryUsageDeserializer());
        simpleModule.addDeserializer(ThroughputQueueBroadcast.class, new ThroughputQueueDeserializer());
        simpleModule.addDeserializer(StreamsTaskCounterBroadcast.class, new StreamsTaskCounterDeserializer());
        simpleModule.addDeserializer(DatumStatusCounterBroadcast.class, new DatumStatusCounterDeserializer());

        objectMapper.registerModule(simpleModule);
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    /**
     * Get all relevant JMX beans, convert their values to strings, and then persist them
     */
    @Override
    public void run() {
        LOGGER.info("BroadcastMonitorThread running");
        while(keepRunning) {
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

                messagePersister.persistMessages(messages);
                Thread.sleep(waitTime);
            } catch (InterruptedException e) {
                LOGGER.error("Interrupted!: {}", e);
                Thread.currentThread().interrupt();
                this.keepRunning = false;
            } catch (Exception e) {
                LOGGER.error("Exception: {}", e);
                this.keepRunning = false;
            }
        }
    }

    /**
     * Go through streams config and set the broadcastURI (if present)
     */
    private void setBroadcastURI() {
        if(streamConfig != null &&
                streamConfig.containsKey("broadcastURI") &&
                streamConfig.get("broadcastURI") != null &&
                streamConfig.get("broadcastURI") instanceof String) {
            try {
                broadcastURI = new URI(streamConfig.get("broadcastURI").toString());
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Go through streams config and set the thread's wait time (if present)
     */
    private void setWaitTime() {
        try {
            if (streamConfig != null &&
                    streamConfig.containsKey("monitoring_broadcast_interval_ms") &&
                    streamConfig.get("monitoring_broadcast_interval_ms") != null &&
                    (streamConfig.get("monitoring_broadcast_interval_ms") instanceof Long ||
                    streamConfig.get("monitoring_broadcast_interval_ms") instanceof Integer)) {
                waitTime = Long.parseLong(streamConfig.get("monitoring_broadcast_interval_ms").toString());
            } else {
                waitTime = DEFAULT_WAIT_TIME;
            }

            //Shutdown
            if(waitTime == -1) {
                this.keepRunning = false;
            }
        } catch (Exception e) {
            LOGGER.error("Exception while trying to set default broadcast thread wait time: {}", e);
        }
    }

    public void shutdown() {
        this.keepRunning = false;
        LOGGER.debug("Shutting down BroadcastMonitor Thread");
    }

    public String getBroadcastURI() {
        return broadcastURI.toString();
    }

    public long getWaitTime() {
        return waitTime;
    }

    public long getDefaultWaitTime() {
        return DEFAULT_WAIT_TIME;
    }

    public void setDefaultWaitTime(long defaultWaitTime) {
        this.DEFAULT_WAIT_TIME = defaultWaitTime;
    }
}