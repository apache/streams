package org.apache.streams.local.counters;

import javax.management.MBeanAttributeInfo;
import javax.management.MBeanInfo;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import java.util.Arrays;

public class MemoryUsagePOJO {
    private boolean verbose;
    private int objectPendingFinalizationCount;
    private long heapMemoryUsage;
    private long nonHeapMemoryUsage;

    private String name;

    public MemoryUsagePOJO(MBeanServer server, MBeanInfo mBeanInfo, ObjectName name) {
        setName(name.getCanonicalName());

        for (MBeanAttributeInfo attribute : Arrays.asList(mBeanInfo.getAttributes())) {
            try {
                switch(attribute.getName()) {
                    case "Verbose":
                        setVerbose((boolean) server.getAttribute(name, attribute.getName()));
                        break;
                    case "ObjectPendingFinalizationCount":
                        setObjectPendingFinalizationCount((int) server.getAttribute(name, attribute.getName()));
                        break;
                    case "HeapMemoryUsage":
                        setHeapMemoryUsage((long) server.getAttribute(name, attribute.getName()));
                        break;
                    case "NonHeapMemoryUsage":
                        setNonHeapMemoryUsage((long) server.getAttribute(name, attribute.getName()));
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

    public boolean isVerbose() {
        return verbose;
    }

    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
    }

    public int getObjectPendingFinalizationCount() {
        return objectPendingFinalizationCount;
    }

    public void setObjectPendingFinalizationCount(int objectPendingFinalizationCount) {
        this.objectPendingFinalizationCount = objectPendingFinalizationCount;
    }

    public long getHeapMemoryUsage() {
        return heapMemoryUsage;
    }

    public void setHeapMemoryUsage(long heapMemoryUsage) {
        this.heapMemoryUsage = heapMemoryUsage;
    }

    public long getNonHeapMemoryUsage() {
        return nonHeapMemoryUsage;
    }

    public void setNonHeapMemoryUsage(long nonHeapMemoryUsage) {
        this.nonHeapMemoryUsage = nonHeapMemoryUsage;
    }

    @Override
    public String toString() {
        return String.format("{\"name\" : \"%s\", \"verbose\" : %s, \"objectPendingFinalizationCount\" : %s, \"heapMemoryUsage\" : %s, \"nonHeapMemoryUsage\" : %s}",
                name, verbose, objectPendingFinalizationCount, heapMemoryUsage, nonHeapMemoryUsage);
    }
}
