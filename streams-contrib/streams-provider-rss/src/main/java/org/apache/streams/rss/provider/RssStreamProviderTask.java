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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.io.FeedException;
import com.sun.syndication.io.SyndFeedInput;
import com.sun.syndication.io.XmlReader;
import org.apache.streams.core.StreamsDatum;
import org.apache.streams.data.util.RFC3339Utils;
import org.apache.streams.rss.FeedDetails;
import org.apache.streams.rss.serializer.SyndEntrySerializer;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A {@link java.lang.Runnable} task that queues rss feed data.
 *
 * <code>RssStreamProviderTask</code> reads the content of an rss feed and queues the articles from
 * the feed inform of a {@link com.fasterxml.jackson.databind.node.ObjectNode} wrapped in a {@link org.apache.streams.core.StreamsDatum}.
 * The task can filter articles by a published date.  If the task cannot parse the date of the article or the article does not contain a
 * published date, by default the task will attempt to queue article.
 *
 * A task can be run in perpetual mode which will store the article urls in a static variable.  The next time a <code>RssStreamProviderTask</code>
 * is run, it will not queue data that was seen the previous time the rss feed was read.  This is an attempt to reduce
 * multiple copies of an article from being out put by a {@link org.apache.streams.rss.provider.RssStreamProvider}.
 *
 * ** Warning! **
 * It still is possible to output multiples of the same article.  If multiple tasks executions for the same rss feed overlap
 * in execution time, it possible that the previously seen articles static variable will not have been updated in time.
 *
 */
public class RssStreamProviderTask implements Runnable {

    private final static Logger LOGGER = LoggerFactory.getLogger(RssStreamProviderTask.class);
    private static final int DEFAULT_TIME_OUT = 10000; // 10 seconds
    private static final String RSS_KEY = "rssFeed";
    private static final String URI_KEY = "uri";
    private static final String LINK_KEY = "link";
    private static final String DATE_KEY = "publishedDate";

    /**
     * Map that contains the Set of previously seen articles by an rss feed.
     */
    @VisibleForTesting
    protected static final Map<String, Set<String>> PREVIOUSLY_SEEN = new ConcurrentHashMap<>();


    private BlockingQueue<StreamsDatum> dataQueue;
    private String rssFeed;
    private int timeOut;
    private SyndEntrySerializer serializer;
    private DateTime publishedSince;
    private boolean perpetual;


    /**
     * Non-perpetual mode, no date filter, time out of 10 sec
     * @see {@link org.apache.streams.rss.provider.RssStreamProviderTask#RssStreamProviderTask(java.util.concurrent.BlockingQueue, String, org.joda.time.DateTime, int, boolean)}
     * @param queue
     * @param rssFeed
     */
    public RssStreamProviderTask(BlockingQueue<StreamsDatum> queue, String rssFeed) {
        this(queue, rssFeed, new DateTime().minusYears(30), DEFAULT_TIME_OUT, false);
    }

    /**
     * Non-perpetual mode, no date filter
     * @see {@link org.apache.streams.rss.provider.RssStreamProviderTask#RssStreamProviderTask(java.util.concurrent.BlockingQueue, String, org.joda.time.DateTime, int, boolean)}
     * @param queue
     * @param rssFeed
     * @param timeOut
     */
    public RssStreamProviderTask(BlockingQueue<StreamsDatum> queue, String rssFeed, int timeOut) {
        this(queue, rssFeed, new DateTime().minusYears(30), timeOut, false);
    }

    /**
     * Non-perpetual mode, time out of 10 sec
     * @see {@link org.apache.streams.rss.provider.RssStreamProviderTask#RssStreamProviderTask(java.util.concurrent.BlockingQueue, String, org.joda.time.DateTime, int, boolean)}
     * @param queue
     * @param rssFeed
     * @param publishedSince
     */
    public RssStreamProviderTask(BlockingQueue<StreamsDatum> queue, String rssFeed, DateTime publishedSince) {
        this(queue, rssFeed, publishedSince, DEFAULT_TIME_OUT, false);
    }

    /**
     * RssStreamProviderTask that reads an rss feed url and queues the resulting articles as StreamsDatums with the documents
     * being object nodes.
     * @param queue Queue to push data to
     * @param rssFeed url of rss feed to read
     * @param publishedSince DateTime to filter articles by, will queue articles with published times after this
     * @param timeOut url connection timeout in milliseconds
     * @param perpetual true, if you want to run in perpetual mode. NOT RECOMMENDED
     */
    public RssStreamProviderTask(BlockingQueue<StreamsDatum> queue, String rssFeed, DateTime publishedSince, int timeOut, boolean perpetual) {
        this.dataQueue = queue;
        this.rssFeed = rssFeed;
        this.timeOut = timeOut;
        this.publishedSince = publishedSince;
        this.serializer = new SyndEntrySerializer();
        this.perpetual = perpetual;
    }

    /**
     * The rss feed url that this task is responsible for reading
     * @return rss feed url
     */
    public String getRssFeed() {
        return this.rssFeed;
    }

    @Override
    public void run() {
        try {
            Set<String> batch = queueFeedEntries(new URL(this.rssFeed));
            if(this.perpetual)
                PREVIOUSLY_SEEN.put(this.getRssFeed(), batch);
        } catch (IOException | FeedException e) {
            LOGGER.warn("Exception while reading rss stream, {} : {}", this.rssFeed, e);
        }
    }

    /**
     * Reads the url and queues the data
     * @param feedUrl rss feed url
     * @return set of all article urls that were read from the feed
     * @throws IOException when it cannot connect to the url or the url is malformed
     * @throws FeedException when it cannot reed the feed.
     */
    @VisibleForTesting
    protected Set<String> queueFeedEntries(URL feedUrl) throws IOException, FeedException {
        Set<String> batch = Sets.newConcurrentHashSet();
        URLConnection connection = feedUrl.openConnection();
        connection.setConnectTimeout(this.timeOut);
        connection.setConnectTimeout(this.timeOut);
        SyndFeedInput input = new SyndFeedInput();
        SyndFeed feed = input.build(new InputStreamReader(connection.getInputStream()));
        for (Object entryObj : feed.getEntries()) {
            SyndEntry entry = (SyndEntry) entryObj;
            ObjectNode nodeEntry = this.serializer.deserialize(entry);
            nodeEntry.put(RSS_KEY, this.rssFeed);
            String entryId = determineId(nodeEntry);
            batch.add(entryId);
            StreamsDatum datum = new StreamsDatum(nodeEntry);
            try {
                JsonNode published = nodeEntry.get(DATE_KEY);
                if (published != null) {
                    try {
                        DateTime date = RFC3339Utils.parseToUTC(published.asText());
                        if (date.isAfter(this.publishedSince) && (!this.perpetual || !seenBefore(entryId, this.rssFeed))) {
                            this.dataQueue.put(datum);
                            LOGGER.debug("Added entry, {}, to provider queue.", entryId);
                        }
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                    } catch (Exception e) {
                        LOGGER.trace("Failed to parse date from object node, attempting to add node to queue by default.");
                        if(!this.perpetual || !seenBefore(entryId, this.rssFeed)) {
                            this.dataQueue.put(datum);
                            LOGGER.debug("Added entry, {}, to provider queue.", entryId);
                        }
                    }
                } else {
                    LOGGER.debug("No published date present, attempting to add node to queue by default.");
                    if(!this.perpetual || !seenBefore(entryId, this.rssFeed)) {
                        this.dataQueue.put(datum);
                        LOGGER.debug("Added entry, {}, to provider queue.", entryId);
                    }
                }
            } catch (InterruptedException ie) {
                LOGGER.error("Interupted Exception.");
                Thread.currentThread().interrupt();
            }
        }
        return batch;
    }

    /**
     * Returns a link to the article to use as the id
     * @param node
     * @return
     */
    private String determineId(ObjectNode node) {
        String id = null;
        if(node.get(URI_KEY) != null && !node.get(URI_KEY).textValue().equals("")) {
            id = node.get(URI_KEY).textValue();
        } else if(node.get(LINK_KEY) != null && !node.get(LINK_KEY).textValue().equals("")) {
            id = node.get(LINK_KEY).textValue();
        }
        return id;
    }

    /**
     * Returns false if the artile was previously seen in another task for this feed
     * @param id
     * @param rssFeed
     * @return
     */
    private boolean seenBefore(String id, String rssFeed) {
        Set<String> previousBatch = PREVIOUSLY_SEEN.get(rssFeed);
        if(previousBatch == null) {
            return false;
        }
        return previousBatch.contains(id);
    }


}
