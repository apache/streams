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
package org.apache.streams.builders.threaded;

import org.apache.streams.core.StreamsDatum;

import java.util.Map;

/**
 * NOT USED.  When joins/partions are implemented, a similar pattern could be followed. Done only as basic proof
 * of concept.
 */
public class StreamsMergeTask extends BaseStreamsTask {


    public StreamsMergeTask() {
        super(null, null, null, null);
    }

    public StreamsMergeTask(String id, Map<String, BaseStreamsTask> ctx) {
        super(id, null, ctx, null);
    }

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
        while(shouldKeepRunning()) {
            StreamsDatum datum = super.pollNextDatum();
            if(datum != null) {
                super.addToOutgoingQueue(datum);
            }
            else {
                Thread.yield();
            }
        }
    }

    public StatusCounts getCurrentStatus() {
        return new StatusCounts(0, 0, 0, 0);
    }



}
