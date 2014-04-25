package org.apache.streams.local.tasks;

/**
 * Created by mhager on 4/25/14.
 */
public interface StreamsMonitorThread extends Runnable
{
    void shutdown();
}
