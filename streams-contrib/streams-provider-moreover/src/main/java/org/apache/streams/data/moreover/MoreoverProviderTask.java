package org.apache.streams.data.moreover;

/*
 * #%L
 * streams-provider-moreover
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

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import org.apache.streams.core.StreamsDatum;
import org.apache.streams.core.StreamsResultSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;
import java.util.Queue;

/**
 * Task to pull from the Morever API
 */
public class MoreoverProviderTask implements Runnable {

    public static final int LATENCY = 10;
    public static final int REQUIRED_LATENCY = LATENCY * 1000;
    private static Logger logger = LoggerFactory.getLogger(MoreoverProviderTask.class);

    private String lastSequence;
    private final String apiKey;
    private final String apiId;
    private final Queue<StreamsDatum> results;
    private final MoreoverClient moClient;
    private boolean started = false;

    public MoreoverProviderTask(String apiId, String apiKey, Queue<StreamsDatum> results, String lastSequence) {
        //logger.info("Constructed new task {} for {} {} {}", UUID.randomUUID().toString(), apiId, apiKey, lastSequence);
        this.apiId = apiId;
        this.apiKey = apiKey;
        this.results = results;
        this.lastSequence = lastSequence;
        this.moClient = new MoreoverClient(this.apiId, this.apiKey, this.lastSequence);
        initializeClient(moClient);
    }

    @Override
    public void run() {
        while(true) {
            try {
                ensureTime(moClient);
                MoreoverResult result = moClient.getArticlesAfter(lastSequence, 500);
                started = true;
                lastSequence = result.process().toString();
                for(StreamsDatum entry : ImmutableSet.copyOf(result.iterator()))
                    results.offer(entry);
                logger.info("ApiKey={}\tlastSequenceid={}", this.apiKey, lastSequence);

            } catch (Exception e) {
                logger.error("Exception while polling moreover", e);
            }
        }
    }

    private void ensureTime(MoreoverClient moClient) {
        try {
            long gap = System.currentTimeMillis() - moClient.pullTime;
            if (gap < REQUIRED_LATENCY)
                Thread.sleep(REQUIRED_LATENCY - gap);
        } catch (Exception e) {
            logger.warn("Error sleeping for latency");
        }
    }

    private void initializeClient(MoreoverClient moClient) {
        try {
            moClient.getArticlesAfter(this.lastSequence, 2);
        } catch (Exception e) {
            logger.error("Failed to start stream, {}", this.apiKey);
            logger.error("Exception : ", e);
            throw new IllegalStateException("Unable to initialize stream", e);
        }
    }
}
