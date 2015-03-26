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

package org.apache.streams.data.moreover;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.moreover.api.Article;
import org.apache.streams.core.StreamsDatum;
import org.apache.streams.core.StreamsResultSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.math.BigInteger;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Task to pull from the Morever API
 */
public class MoreoverProviderTask implements Runnable {

    public static final int LATENCY = 20; // required by moreover
    public static final int REQUIRED_LATENCY = LATENCY * 1000;
    private static final Logger LOGGER = LoggerFactory.getLogger(MoreoverProviderTask.class);
    private static final int MAX_ATTEMPTS = 5;

    private String lastSequence;
    private final String apiKey;
    private final String apiId;
    private final BlockingQueue<StreamsDatum> results;
    private AtomicBoolean keepRunning;
    private AtomicBoolean running;
    private boolean perpetualMode;

    /**
     *
     * @param apiId
     * @param apiKey
     * @param results
     * @param lastSequence
     * @param perpetualMode true if this should continuously poll moreover for new data
     */
    public MoreoverProviderTask(String apiId, String apiKey, BlockingQueue<StreamsDatum> results, String lastSequence, boolean perpetualMode) {
        //logger.info("Constructed new task {} for {} {} {}", UUID.randomUUID().toString(), apiId, apiKey, lastSequence);
        this.apiId = apiId;
        this.apiKey = apiKey;
        this.results = results;
        this.lastSequence = lastSequence;
        this.perpetualMode = perpetualMode;
        this.keepRunning = new AtomicBoolean(true);
        this.running = new AtomicBoolean(false);
    }

    /**
     * Stops the task from polling the moreover api again.  Task will finish any polling its in the middle of and add all results
     * to the results queue.
     */
    public void stopTask() {
        this.keepRunning.set(false);
    }

    /**
     * Returns true if the task is getting results from the moreover api.  Returns false when the task
     * will not poll the api any more and has add all results to the outgoing queue.
     * @return
     */
    public boolean isRunning() {
        return this.running.get();
    }

    /**
     * Gets a moreover client
     * @param apiId
     * @param apiKey
     * @param lastSequence
     * @return
     */
    @VisibleForTesting
    protected MoreoverClient getMoreoverClient(String apiId, String apiKey, String lastSequence) {
        return new MoreoverClient(apiId, apiKey, lastSequence);
    }

    /**
     * Polls the moreover api till it returns zero results.  If it is in perpetual mode it will then continue to poll on a
     * cadence.  If not in perpetual mode it will exit the method.
     */
    @Override
    public void run() {
        this.running.set(true);
        try {
            // have to poll once to ensure initial sequence id
            MoreoverClient moreoverClient = getMoreoverClient(this.apiId, this.apiKey, this.lastSequence);
            MoreoverResult initialResult = moreoverClient.getNextBatch();
            LOGGER.debug("Adding {} articles to the output queue.", initialResult.numArticles());
            this.lastSequence = initialResult.getMaxSequencedId().toString();
            addMoreoverArticlesToOutputQueue(initialResult);
            pollTillClientReturnsLessThanMaxResults(moreoverClient);
            if (this.perpetualMode) {
                perpetualRun(moreoverClient);
            }
        } catch (IOException ioe) {
            LOGGER.error("Unable to initialize moreover data with the api. apiId={} apiKey={} lastSequence={}", this.apiId, this.apiKey, this.lastSequence);
            LOGGER.error("Unable to initialize moreover data : {}", ioe);
        } catch (InterruptedException ie) {
            LOGGER.warn("Caught InteruptedException. Shutting down.", ie);
            Thread.currentThread().interrupt();
        } finally {
            this.running.set(false);
        }
    }

    /**
     * Polls till the moreover client returns less than max result
     * @param client
     * @throws InterruptedException
     */
    protected void pollTillClientReturnsLessThanMaxResults(MoreoverClient client) throws InterruptedException {
        int resultSize = 0;
        int attempt = 0;
        do {
            try {
                ensureTime(client);
                MoreoverResult result = client.getNextBatch();
                LOGGER.debug("Adding {} articles to the output queue.", result.numArticles());
                addMoreoverArticlesToOutputQueue(result);
                resultSize = result.numArticles();
                attempt = 0; //reset attempt
            } catch (IOException ioe) {
                ++attempt;
                if(attempt >= MAX_ATTEMPTS) {
                    LOGGER.error("Unable to get api response after {} attempts. Shutting down", attempt);
                    LOGGER.error("Unable to connect to moreover api : {}", ioe);
                    this.keepRunning.set(false);
                } else {
                    LOGGER.warn("Unable to connect to moreover api: {}", ioe);
                    LOGGER.warn("Will retry in 1 second");
                    Thread.currentThread().sleep(1000);
                }
            }
        }
        while (this.keepRunning.get() && resultSize == MoreoverClient.MAX_LIMIT &&
                attempt < MAX_ATTEMPTS && !Thread.currentThread().isInterrupted());
        LOGGER.debug("Exiting with after recieving result size of {}", resultSize);
    }

    protected void addMoreoverArticlesToOutputQueue(MoreoverResult result) throws InterruptedException {
        for (Article moreoverArticle : result.getArticles()) {
            this.results.put(new StreamsDatum(moreoverArticle, moreoverArticle.getId()));
        }
    }

    protected void perpetualRun(MoreoverClient client) throws InterruptedException {
        while(this.keepRunning.get() && !Thread.currentThread().isInterrupted()) {
            ensureTime(client);
            pollTillClientReturnsLessThanMaxResults(client);
        }
    }

    private void ensureTime(MoreoverClient moClient) throws InterruptedException {
        long gap = System.currentTimeMillis() - moClient.getPullTime();
        if (gap < REQUIRED_LATENCY) {
            Thread.sleep(REQUIRED_LATENCY - gap);
        }
    }


}
