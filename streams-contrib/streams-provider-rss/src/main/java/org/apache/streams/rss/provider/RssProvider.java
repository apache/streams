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
import com.google.common.collect.Sets;
import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.io.FeedException;
import com.sun.syndication.io.SyndFeedInput;
import org.apache.streams.rss.serializer.SyndEntrySerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;

public abstract class RssProvider {
    private final static Logger LOGGER = LoggerFactory.getLogger(RssProvider.class);

    protected Queue<ObjectNode> entries;
    protected boolean perpetual = true;
    protected SyndEntrySerializer serializer;
    protected ExecutorService executor;
    protected AtomicBoolean keepRunning;
    protected AtomicBoolean doneProviding;
    protected Set<String> previouslySeen;

    public RssProvider() {
    }

    /**
     * Polls all of the rss feeds and adds the results to the the queue of SyndEntries
     * @param rssFeeds rss feeds to read and processes
     */
    public void pullRssData(Set<String> rssFeeds) {
        if(this.previouslySeen == null) {
            this.previouslySeen = Sets.newHashSet();
        }

        for (String rssFeed : rssFeeds) {
            LOGGER.debug("Attempting to read rss feed : {}", rssFeed);
            LOGGER.debug("whatever");
            try {
                URL feedUrl = new URL(rssFeed);
                pullData(feedUrl);
            } catch (Exception e) {
                LOGGER.error("Failed to connect to a read rss feed : {}", rssFeed);
                LOGGER.error("Exception while trying to connect and read feed : {}", e);
            }
        }

        if(this.perpetual) {
            this.doneProviding.set(true);
        }
    }

    /**
     * Adds the data from a feed urls to the queue of SyndEntries. Only queues the entries that were not seen in last
     * poll request.
     * @param feedUrl
     * @throws java.io.IOException
     * @throws com.sun.syndication.io.FeedException
     */
    public void pullData(URL feedUrl) throws IOException, FeedException {
        SyndFeedInput input = new SyndFeedInput();
        URLConnection connection = feedUrl.openConnection();
        connection.setConnectTimeout(10000);
        connection.setReadTimeout(10000);
        SyndFeed feed = buildFeed(feedUrl);//input.build(new InputStreamReader(connection.getInputStream()));

        for(Object entryObj : feed.getEntries()) {
            SyndEntry entry = (SyndEntry) entryObj;
            ObjectNode nodeEntry = this.serializer.deserialize(entry);
            nodeEntry.put("rssFeed", feedUrl.toString());

            if(!continuePulling(nodeEntry)) {
                break;
            }

            synchronized (this.entries) {
                while(!this.entries.offer(nodeEntry)) {
                    Thread.yield();
                }
            }
        }
    }

    protected SyndFeed buildFeed(URL feedUrl) {
        SyndFeed feed = null;

        try {
            SyndFeedInput input = new SyndFeedInput();

            URLConnection connection = feedUrl.openConnection();
            connection.setConnectTimeout(10000);
            connection.setReadTimeout(10000);

            feed = input.build(new InputStreamReader(connection.getInputStream()));
        } catch (Exception e) {
            LOGGER.error("Exception while trying to build RSS feed for: {}, {}", feedUrl, e);
        }

        return feed;
    }

    /**
     * Given a RSS document, determine whether or not we need to continue pulling articles
     * @param obj
     * @return boolean Whether or not we need to continue pulling from this feed. Dependent on latest doc in ES
     */
    protected abstract boolean continuePulling(ObjectNode obj);
}