package org.apache.streams.local.tasks;

/*
 * #%L
 * streams-runtime-local
 * %%
 * Copyright (C) 2013 - 2014 Apache Streams Project
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

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
