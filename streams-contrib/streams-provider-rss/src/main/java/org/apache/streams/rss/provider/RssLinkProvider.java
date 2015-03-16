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
package org.apache.streams.rss.provider;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.Queues;
import org.apache.commons.lang.NotImplementedException;
import org.apache.streams.core.StreamsDatum;
import org.apache.streams.core.StreamsProvider;
import org.apache.streams.core.StreamsResultSet;
import org.apache.streams.rss.serializer.SyndEntrySerializer;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

public class RssLinkProvider extends RssProvider implements StreamsProvider {
    private final static Logger LOGGER = LoggerFactory.getLogger(RssProvider.class);
    private Set<String> feeds;

    public RssLinkProvider(Set<String> feeds) {
        this.feeds = feeds;
    }

    @Override
    protected boolean continuePulling(ObjectNode obj) {
        return true;
    }

    @Override
    public void startStream() {
        this.perpetual = true;
        LOGGER.info("** Starting RSS Thread. **");
        this.executor.execute(new PollingTask(this, this.feeds));
    }

    @Override
    public StreamsResultSet readCurrent() {
        Queue<StreamsDatum> result = Queues.newConcurrentLinkedQueue();
        synchronized (this.entries) {
            if(this.entries.isEmpty() && this.doneProviding.get()) {
                this.keepRunning.set(false);
            }

            while(!this.entries.isEmpty()) {
                ObjectNode node = this.entries.poll();

                try {
                    while (!result.offer(new StreamsDatum(node.get("uri").asText()))) {
                        Thread.yield();
                    }
                } catch (Exception e) {
                    LOGGER.error("Problem offering up new StreamsDatum: {}", node.asText());
                }
            }
        }
        LOGGER.debug("** ReadCurrent return {} streams datums", result.size());

        return new StreamsResultSet(result);
    }

    @Override
    public StreamsResultSet readNew(BigInteger bigInteger) {
        throw new NotImplementedException("readNew not implemented for " + this.getClass().toString());
    }

    @Override
    public StreamsResultSet readRange(DateTime dateTime, DateTime dateTime1) {
        throw new NotImplementedException("readRange not implemented for " + this.getClass().toString());
    }

    @Override
    public boolean isRunning() {
        return this.keepRunning.get();
    }

    @Override
    public void prepare(Object o) {
        this.entries = new ConcurrentLinkedQueue<ObjectNode>();
        this.serializer = new SyndEntrySerializer();
        this.executor = Executors.newSingleThreadExecutor();
        this.keepRunning = new AtomicBoolean(true);
        this.doneProviding = new AtomicBoolean(false);
    }

    @Override
    public void cleanUp() {
        if(this.executor != null) {
            this.executor.shutdownNow();
        }
    }

    /**
     * Runnable class that pulls down entries for a given RSS Feed
     */
    private class PollingTask implements Runnable {

        private RssProvider provider;
        private Set<String> feeds;

        public PollingTask(RssProvider provider, Set<String> feeds) {
            this.provider = provider;
            this.feeds = feeds;
        }

        @Override
        public void run() {
            this.provider.pullRssData(this.feeds);
        }
    }
}