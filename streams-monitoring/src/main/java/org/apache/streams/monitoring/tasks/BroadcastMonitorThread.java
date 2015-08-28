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
import org.apache.streams.config.ComponentConfigurator;
import org.apache.streams.config.StreamsConfigurator;
import org.apache.streams.config.StreamsConfiguration;
import org.apache.streams.jackson.*;
import org.apache.streams.local.monitoring.MonitoringConfiguration;
import org.apache.streams.monitoring.persist.MessagePersister;
import org.apache.streams.monitoring.persist.impl.BroadcastMessagePersister;
import org.apache.streams.monitoring.persist.impl.LogstashUdpMessagePersister;
import org.apache.streams.monitoring.persist.impl.SLF4JMessagePersister;
import org.apache.streams.pojo.json.Broadcast;
import org.apache.streams.pojo.json.DatumStatusCounterBroadcast;
import org.apache.streams.pojo.json.MemoryUsageBroadcast;
import org.apache.streams.pojo.json.StreamsTaskCounterBroadcast;
import org.apache.streams.pojo.json.ThroughputQueueBroadcast;
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

    private MonitoringConfiguration configuration;
    private URI broadcastURI = null;
    private MessagePersister messagePersister;
    private volatile boolean keepRunning;

    private static ObjectMapper objectMapper = StreamsJacksonMapper.getInstance();

    /**
     * DEPRECATED
     * Please initialize logging with monitoring object via typesafe
     * @param streamConfig
     */
    @Deprecated
    public BroadcastMonitorThread(Map<String, Object> streamConfig) {
        this(objectMapper.convertValue(streamConfig, MonitoringConfiguration.class));
    }

    public BroadcastMonitorThread(StreamsConfiguration streamConfig) {
        this(objectMapper.convertValue(streamConfig.getAdditionalProperties().get("monitoring"), MonitoringConfiguration.class));
    }

    public BroadcastMonitorThread(MonitoringConfiguration configuration) {

        this.configuration = configuration;
        if( this.configuration == null )
            this.configuration = new ComponentConfigurator<>(MonitoringConfiguration.class).detectConfiguration(StreamsConfigurator.getConfig().atPath("monitoring"));

        LOGGER.info("BroadcastMonitorThread created");

        initializeObjectMapper();

        prepare();

        LOGGER.info("BroadcastMonitorThread initialized");

    }

    /**
     * Initialize our object mapper with all of our bean's custom deserializers
     * This way we can convert them to and from Strings dictated by our
     * POJOs which are generated from JSON schemas
     */
    private void initializeObjectMapper() {
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
                Thread.sleep(configuration.getMonitoringBroadcastIntervalMs());
            } catch (InterruptedException e) {
                LOGGER.debug("Broadcast Monitor Interrupted!");
                Thread.currentThread().interrupt();
                this.keepRunning = false;
            } catch (Exception e) {
                LOGGER.error("Exception: {}", e);
                this.keepRunning = false;
            }
        }
    }

    public void prepare() {

        keepRunning = true;

        LOGGER.info("BroadcastMonitorThread setup " + this.configuration);

        server = ManagementFactory.getPlatformMBeanServer();

        if (this.configuration != null &&
            this.configuration.getBroadcastURI() != null) {

            try {
                broadcastURI = new URI(configuration.getBroadcastURI());
            } catch (Exception e) {
                LOGGER.error("invalid URI: ", e);
            }

            if (broadcastURI != null) {
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
        } else {
            messagePersister = new SLF4JMessagePersister();
        }

    }

    public void shutdown() {
        this.keepRunning = false;
        LOGGER.debug("Shutting down BroadcastMonitor Thread");
    }

    public String getBroadcastURI() {
        return configuration.getBroadcastURI();
    }

    public long getWaitTime() {
        return configuration.getMonitoringBroadcastIntervalMs();
    }

}
