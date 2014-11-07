/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.streams.local.tasks;

import org.apache.streams.core.StreamsDatum;
import org.apache.streams.local.counters.StreamsTaskCounter;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * NOT USED.  When joins/partions are implemented, a similar pattern could be followed. Done only as basic proof
 * of concept.
 * NEEDS TO BE RE-WRITTEN
 */
@Deprecated
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
                try {
                    super.addToOutgoingQueue(datum);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                }
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

    @Override
    public void setStreamsTaskCounter(StreamsTaskCounter counter) {
        throw new NotImplementedException();
    }
}
