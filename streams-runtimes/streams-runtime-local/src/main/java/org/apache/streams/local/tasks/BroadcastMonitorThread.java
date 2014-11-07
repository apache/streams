package org.apache.streams.local.tasks;

import com.google.common.collect.Lists;
import org.apache.streams.local.counters.DatumStatusCounterPOJO;
import org.apache.streams.local.counters.StreamsTaskCounterPOJO;
import org.apache.streams.local.queues.ThroughputQueuePOJO;
import org.slf4j.Logger;

import javax.management.*;
import java.lang.management.ManagementFactory;
import java.util.List;
import java.util.Set;

public class BroadcastMonitorThread extends NotificationBroadcasterSupport implements Runnable {
    private static final Logger LOGGER = org.slf4j.LoggerFactory.getLogger(BroadcastMonitorThread.class);
    private static MBeanServer server;
    private long DEFAULT_WAIT_TIME = 30000;

    public BroadcastMonitorThread() {
        server = ManagementFactory.getPlatformMBeanServer();
    }

    @Override
    public void run() {
        while(true) {
            try {
                List<String> messages = Lists.newArrayList();
                Set<ObjectName> beans = server.queryNames(null, null);

                for(ObjectName name : beans) {
                    if(name.getKeyPropertyList().get("type") != null && name.getKeyPropertyList().get("type").equals("ThroughputQueue")) {
                        MBeanInfo info = server.getMBeanInfo(name);
                        messages.add((new ThroughputQueuePOJO(server, info, name)).toString());
                    } else if(name.getKeyPropertyList().get("type") != null && name.getKeyPropertyList().get("type").equals("StreamsTaskCounter")) {
                        MBeanInfo info = server.getMBeanInfo(name);
                        messages.add((new StreamsTaskCounterPOJO(server, info, name)).toString());
                    } else if(name.getKeyPropertyList().get("type") != null && name.getKeyPropertyList().get("type").equals("DatumStatusCounter")) {
                        MBeanInfo info = server.getMBeanInfo(name);
                        messages.add((new DatumStatusCounterPOJO(server, info, name)).toString());
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