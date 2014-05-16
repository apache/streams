package org.apache.streams.local.monitors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryUsage;
import java.util.TimerTask;

public class MonitorJVMTask extends TimerTask {

    private static final Logger LOGGER = LoggerFactory.getLogger(MonitorJVMTask.class);

    public MonitorJVMTask() {

    }

    public void run() {
        /**
         *
         * Note:
         * Quick class and method to let us see what is going on with the JVM. We need to make sure
         * that everything is running with as little memory as possible. If we are generating a heap
         * overflow, this will be very apparent by the information shown here.
         */
        MemoryUsage memoryUsage = ManagementFactory.getMemoryMXBean().getHeapMemoryUsage();

        String maxMemory = memoryUsage.getMax() == Long.MAX_VALUE ? "NO_LIMIT" :
                humanReadableByteCount(memoryUsage.getMax(), true);

        String usedMemory = humanReadableByteCount(memoryUsage.getUsed(), true);

        LOGGER.info("[monitor] Used Memory: {}, Max: {}",
                usedMemory,
                maxMemory);
    }

    public String humanReadableByteCount(long bytes, boolean si) {
        int unit = si ? 1000 : 1024;
        if (bytes < unit) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(unit));
        String pre = (si ? "kMGTPE" : "KMGTPE").charAt(exp - 1) + (si ? "" : "i");
        return String.format("%.1f %sB", bytes / Math.pow(unit, exp), pre);
    }

}
