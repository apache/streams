package org.apache.streams.local.tasks;

import org.apache.streams.core.StreamsDatum;

import java.util.Map;

/**
 * NOT USED.  When joins/partions are implemented, a similar pattern could be followed. Done only as basic proof
 * of concept.
 */
public class StreamsMergeTask extends BaseStreamsTask {

    @Override
    public void setStreamConfig(Map<String, Object> config) {
        // no Operation
    }

    @Override
    public boolean isRunning() {
        return false;
    }

    @Override
    public void run() {
        while(this.keepRunning.get()) {
            StreamsDatum datum = super.pollNextDatum();
            if(datum != null) {
                super.addToOutgoingQueue(datum);
            }
            else {
                safeQuickRest();
            }
        }
    }
}
