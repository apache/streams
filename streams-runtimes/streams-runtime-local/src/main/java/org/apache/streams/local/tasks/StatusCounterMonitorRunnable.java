package org.apache.streams.local.tasks;

public interface StatusCounterMonitorRunnable extends Runnable {
    void shutdown();
    boolean isRunning();
}
