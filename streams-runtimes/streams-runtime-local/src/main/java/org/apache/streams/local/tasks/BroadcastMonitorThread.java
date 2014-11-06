package org.apache.streams.local.tasks;

import org.slf4j.Logger;

public class BroadcastMonitorThread extends Thread {
    private static final Logger LOGGER = org.slf4j.LoggerFactory.getLogger(BroadcastMonitorThread.class);

    public BroadcastMonitorThread() { }

    @Override
    public void run() {
        LOGGER.debug("OMFG BROADCAST THREAD");
    }
}
