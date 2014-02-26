package org.apache.streams.core.tasks;

import org.apache.streams.core.StreamsDatum;

import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * NOT USED.  When joins/partions are implemented, a similar pattern could be followed. Done only as basic proof
 * of concept.
 */
public class StreamsMergeTask extends BaseStreamsTask {

    private AtomicBoolean keepRunning;
    private long sleepTime;

    public StreamsMergeTask() {
        this(DEFAULT_SLEEP_TIME_MS);
    }

    public StreamsMergeTask(long sleepTime) {
        this.sleepTime = sleepTime;
        this.keepRunning = new AtomicBoolean(true);
    }


    @Override
    public void stopTask() {
        this.keepRunning.set(false);
    }

    @Override
    public void setStreamConfig(Map<String, Object> config) {

    }

    @Override
    public boolean isRunning() {
        return false;
    }

    @Override
    public void run() {
        while(this.keepRunning.get()) {
            StreamsDatum datum = super.getNextDatum();
            if(datum != null) {
                super.addToOutgoingQueue(datum);
            }
            else {
                try {
                    Thread.sleep(this.sleepTime);
                } catch (InterruptedException e) {
                    this.keepRunning.set(false);
                }
            }
        }
    }
}
