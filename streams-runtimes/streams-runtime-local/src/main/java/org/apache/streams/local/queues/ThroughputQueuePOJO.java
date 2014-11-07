package org.apache.streams.local.queues;

import javax.management.MBeanAttributeInfo;
import javax.management.MBeanInfo;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import java.util.Arrays;

public class ThroughputQueuePOJO {
    private long currentSize;
    private double avgWait;
    private long maxWait;
    private long removed;
    private long added;
    private double throughput;
    private String name;

    public ThroughputQueuePOJO(MBeanServer server, MBeanInfo mBeanInfo, ObjectName name) {
        setName(name.getCanonicalName());

        for (MBeanAttributeInfo attribute : Arrays.asList(mBeanInfo.getAttributes())) {
            try {
                switch(attribute.getName()) {
                    case "CurrentSize":
                        setCurrentSize((long)server.getAttribute(name, attribute.getName()));
                        break;
                    case "AvgWait":
                        setAvgWait((double) server.getAttribute(name, attribute.getName()));
                        break;
                    case "MaxWait":
                        setMaxWait((long) server.getAttribute(name, attribute.getName()));
                        break;
                    case "Removed":
                        setRemoved((long) server.getAttribute(name, attribute.getName()));
                        break;
                    case "Added":
                        setAdded((long) server.getAttribute(name, attribute.getName()));
                        break;
                    case "Throughput":
                        setThroughput((double)server.getAttribute(name, attribute.getName()));
                        break;
                }
            } catch (Exception e) {

            }
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getCurrentSize() {
        return currentSize;
    }

    public void setCurrentSize(long currentSize) {
        this.currentSize = currentSize;
    }

    public double getAvgWait() {
        return avgWait;
    }

    public void setAvgWait(double avgWait) {
        this.avgWait = avgWait;
    }

    public long getMaxWait() {
        return maxWait;
    }

    public void setMaxWait(long maxWait) {
        this.maxWait = maxWait;
    }

    public long getRemoved() {
        return removed;
    }

    public void setRemoved(long removed) {
        this.removed = removed;
    }

    public long getAdded() {
        return added;
    }

    public void setAdded(long added) {
        this.added = added;
    }

    public double getThroughput() {
        return throughput;
    }

    public void setThroughput(double throughput) {
        this.throughput = throughput;
    }

    @Override
    public String toString() {
        return String.format("{\"name\" : \"%s\", \"currentSize\" : %s, \"avgWait\" : %s, \"maxWait\" : %s, \"removed\" : %s, \"added\" : %s, \"throughput\" : %s}",
                name, currentSize, avgWait, maxWait, removed, added, throughput);
    }
}
