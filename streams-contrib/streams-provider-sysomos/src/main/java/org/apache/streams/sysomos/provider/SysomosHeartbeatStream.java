/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
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

package org.apache.streams.sysomos.provider;

import com.sysomos.xml.BeatApi;
import org.apache.streams.core.StreamsDatum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides a {@link java.lang.Runnable} query mechanism for grabbing documents from the Sysomos API
 */
public class SysomosHeartbeatStream implements Runnable {

    private final static Logger LOGGER = LoggerFactory.getLogger(SysomosHeartbeatStream.class);

    private final SysomosProvider provider;
    private final SysomosClient client;
    private final String heartbeatId;
    private final long maxApiBatch;
    private final long minLatency;

    private String lastID = null;

    public SysomosHeartbeatStream(SysomosProvider provider, String heartbeatId) {
        this.provider = provider;
        this.heartbeatId = heartbeatId;

        this.client = provider.getClient();
        this.maxApiBatch = provider.getMaxApiBatch();
        this.minLatency = provider.getMinLatency();
    }

    @Override
    public void run() {
        QueryResult result;
        //Iff we are trying to get to a specific document ID, continue to query after minimum delay
        do {
            LOGGER.debug("Querying API to match last ID of {}", lastID);
            result = executeAPIRequest();
            sleep();
        } while (lastID != null && !result.isMatchedLastId());
        //Set the last ID so that the next time we are executed we will continue to query only so long as we haven't
        //found the specific ID
        lastID = result.getCurrentId();
        LOGGER.debug("Completed current execution with a final docID of {}", lastID);
    }

    protected void sleep() {
        try {
            Thread.sleep(this.minLatency);
        } catch (InterruptedException e) {
            LOGGER.warn("Thread interrupted while sleeping minimum delay", e);
        }
    }

    protected QueryResult executeAPIRequest() {
        BeatApi.BeatResponse response = null;
        try {
            response = this.client.createRequestBuilder()
                    .setHeartBeatId(heartbeatId)
                    .setOffset(0)
                    .setReturnSetSize(maxApiBatch).execute();

            LOGGER.debug("Received {} results from API query", response.getCount());
        } catch (Exception e) {
            LOGGER.warn("Error querying Sysomos API", e);
        }

        String currentId = null;
        boolean matched = false;
        if(response != null) {
            for (BeatApi.BeatResponse.Beat beat : response.getBeat()) {
                String docId = beat.getDocid();
                //We get documents in descending time order.  This will set the id to the latest document
                if (currentId == null) {
                    currentId = docId;
                }
                //We only want to process documents that we know we have not seen before
                if (lastID != null && lastID.equals(docId)) {
                    matched = true;
                    break;
                }
                StreamsDatum item = new StreamsDatum(beat, docId);
                item.getMetadata().put("heartbeat", this.heartbeatId);
                this.provider.enqueueItem(item);
            }
        }
        return new QueryResult(matched, currentId);
    }

    private class QueryResult {
        private boolean matchedLastId;
        private String currentId;

        private QueryResult(boolean matchedLastId, String currentId) {
            this.matchedLastId = matchedLastId;
            this.currentId = currentId;
        }

        public boolean isMatchedLastId() {
            return matchedLastId;
        }

        public void setMatchedLastId(boolean matchedLastId) {
            this.matchedLastId = matchedLastId;
        }

        public String getCurrentId() {
            return currentId;
        }

        public void setCurrentId(String currentId) {
            this.currentId = currentId;
        }
    }
}
