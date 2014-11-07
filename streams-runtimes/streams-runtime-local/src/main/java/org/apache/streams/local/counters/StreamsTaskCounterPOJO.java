package org.apache.streams.local.counters;

import javax.management.MBeanAttributeInfo;
import javax.management.MBeanInfo;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import java.util.Arrays;

public class StreamsTaskCounterPOJO {
    private double errorRate;
    private long numEmitted;
    private long numReceived;
    private long numUnhandledErrors;
    private double avgTime;
    private long maxTime;
    private String name;

    public StreamsTaskCounterPOJO(MBeanServer server, MBeanInfo mBeanInfo, ObjectName name) {
        setName(name.getCanonicalName());

        for (MBeanAttributeInfo attribute : Arrays.asList(mBeanInfo.getAttributes())) {
            try {
                switch (attribute.getName()) {
                    case "ErrorRate":
                        setErrorRate((double)server.getAttribute(name, attribute.getName()));
                        break;
                    case "NumEmitted":
                        setNumEmitted((long)server.getAttribute(name, attribute.getName()));
                        break;
                    case "NumReceived":
                        setNumReceived((long)server.getAttribute(name, attribute.getName()));
                        break;
                    case "NumUnhandledErrors":
                        setNumUnhandledErrors((long)server.getAttribute(name, attribute.getName()));
                        break;
                    case "AvgTime":
                        setAvgTime((double)server.getAttribute(name, attribute.getName()));
                        break;
                    case "MaxTime":
                        setMaxTime((long)server.getAttribute(name, attribute.getName()));
                        break;
                }
            } catch (Exception e) {

            }
        }
    }

    public double getErrorRate() {
        return errorRate;
    }

    public void setErrorRate(double errorRate) {
        this.errorRate = errorRate;
    }

    public long getNumEmitted() {
        return numEmitted;
    }

    public void setNumEmitted(long numEmitted) {
        this.numEmitted = numEmitted;
    }

    public long getNumReceived() {
        return numReceived;
    }

    public void setNumReceived(long numReceived) {
        this.numReceived = numReceived;
    }

    public long getNumUnhandledErrors() {
        return numUnhandledErrors;
    }

    public void setNumUnhandledErrors(long numUnhandledErrors) {
        this.numUnhandledErrors = numUnhandledErrors;
    }

    public double getAvgTime() {
        return avgTime;
    }

    public void setAvgTime(double avgTime) {
        this.avgTime = avgTime;
    }

    public long getMaxTime() {
        return maxTime;
    }

    public void setMaxTime(long maxTime) {
        this.maxTime = maxTime;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return String.format("{\"errorRate\" : %s, \"numEmitted\" : %s, \"numReceived\" : %s, \"numUnhandledErrors\" : %s, \"avgTime\" : %s, \"maxTime\" : %s, \"name\" : \"%s\"}",
                errorRate, numEmitted, numReceived, numUnhandledErrors, avgTime, maxTime, name);
    }
}
