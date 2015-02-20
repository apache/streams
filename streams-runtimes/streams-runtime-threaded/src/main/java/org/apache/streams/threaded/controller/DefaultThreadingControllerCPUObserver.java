package org.apache.streams.threaded.controller;

import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import java.lang.management.ManagementFactory;

public class DefaultThreadingControllerCPUObserver implements ThreadingControllerCPUObserver {

    private static final Double FALL_BACK = 0.7;

    @Override
    public double getCPUPercentUtilization() {
        try {
            MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
            ObjectName name = ObjectName.getInstance("java.lang:type=OperatingSystem");

            AttributeList list = mbs.getAttributes(name, new String[]{"ProcessCpuLoad"});

            if (list.isEmpty()) return Double.NaN;

            Attribute att = (Attribute) list.get(0);
            Double value = (Double) att.getValue();

            Double load = ((int)(value * 1000) / 10.0);

            return load > 0 && load < 1.0 ? load : FALL_BACK;
        }
        catch(Throwable t) {
            return FALL_BACK;
        }
    }
}
