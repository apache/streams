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

import javax.management.MBeanServer;
import javax.management.ObjectName;
import java.lang.management.ManagementFactory;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class LocalRuntimeBroadcastMonitorThread {
    private static final Logger LOGGER = org.slf4j.LoggerFactory.getLogger(LocalRuntimeBroadcastMonitorThread.class);
    private static MBeanServer server;

    private long DEFAULT_WAIT_TIME = 30000;
    private long waitTime;
    private ObjectMapper objectMapper;
    private Map<String, Object> streamConfig;
    private URI broadcastURI = null;
    private MessagePersister messagePersister;

    protected volatile boolean keepRunning;

    public LocalRuntimeBroadcastMonitorThread(Map<String, Object> streamConfig) {
        keepRunning = true;
        this.streamConfig = streamConfig;

        LOGGER.info("LocalRuntimeBroadcastMonitorThread starting" + streamConfig);

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
    public void persistMessages() {
        List<String> messages = Lists.newArrayList();
        Set<ObjectName> beans = getServer().queryNames(null, null);

        try {
            for (ObjectName name : beans) {
                String item = getObjectMapper().writeValueAsString(name);
                Broadcast broadcast = null;

                if (name.getKeyPropertyList().get("type") != null) {
                    if (name.getKeyPropertyList().get("type").equals("ThroughputQueue")) {
                        broadcast = getObjectMapper().readValue(item, ThroughputQueueBroadcast.class);
                    } else if (name.getKeyPropertyList().get("type").equals("StreamsTaskCounter")) {
                        broadcast = getObjectMapper().readValue(item, StreamsTaskCounterBroadcast.class);
                    } else if (name.getKeyPropertyList().get("type").equals("DatumCounter")) {
                        broadcast = getObjectMapper().readValue(item, DatumStatusCounterBroadcast.class);
                    } else if (name.getKeyPropertyList().get("type").equals("Memory")) {
                        broadcast = getObjectMapper().readValue(item, MemoryUsageBroadcast.class);
                    }

                    if (broadcast != null) {
                        messages.add(getObjectMapper().writeValueAsString(broadcast));
                    }
                }
            }

            getMessagePersister().persistMessages(messages);
        } catch (Exception e) {
            LOGGER.error("Exception while trying to persist messages: {}", e);
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

    public MBeanServer getServer() {
        return server;
    }

    public ObjectMapper getObjectMapper() {
        return this.objectMapper;
    }

    public MessagePersister getMessagePersister() {
        return this.messagePersister;
    }
}