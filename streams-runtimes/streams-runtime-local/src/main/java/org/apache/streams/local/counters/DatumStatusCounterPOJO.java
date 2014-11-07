package org.apache.streams.local.counters;

import javax.management.MBeanAttributeInfo;
import javax.management.MBeanInfo;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import java.util.Arrays;

public class DatumStatusCounterPOJO {
    private long failed;
    private long passed;
    private String name;

    public DatumStatusCounterPOJO(MBeanServer server, MBeanInfo mBeanInfo, ObjectName name) {
        setName(name.getCanonicalName());

        for (MBeanAttributeInfo attribute : Arrays.asList(mBeanInfo.getAttributes())) {
            try {
                switch(attribute.getName()) {
                    case "Failed":
                        setFailed((long)server.getAttribute(name, attribute.getName()));
                        break;
                    case "Passed":
                        setPassed((long) server.getAttribute(name, attribute.getName()));
                        break;
                }
            } catch (Exception e) {

            }
        }
    }

    public long getFailed() {
        return failed;
    }

    public void setFailed(long failed) {
        this.failed = failed;
    }

    public long getPassed() {
        return passed;
    }

    public void setPassed(long passed) {
        this.passed = passed;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return String.format("{\"name\" : \"%s\", \"passed\" : %s, \"failed\" : %s}",
                name, passed, failed);
    }
}