package org.apache.streams.rss.provider;

import com.google.common.collect.Sets;
import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndFeed;
import org.apache.streams.rss.FeedDetails;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

/**
 * Created by sblackmon on 12/10/13.
 */
public class RssStreamProviderTask implements Runnable {

    private final static Logger LOGGER = LoggerFactory.getLogger(RssStreamProviderTask.class);

    private RssStreamProvider provider;
    private FeedDetails feedDetails;
    private SyndFeed feed;

    private Set<SyndEntry> priorPollResult = Sets.newHashSet();

    public RssStreamProviderTask(RssStreamProvider provider, SyndFeed feed, FeedDetails feedDetails) {
        this.provider = provider;
        this.feed = feed;
        this.feedDetails = feedDetails;
    }

    @Override
    public void run() {

        while(true) {
            try {
                Set<SyndEntry> update = Sets.newHashSet(feed.getEntries());
                Set<SyndEntry> repeats = Sets.intersection(priorPollResult, Sets.newHashSet(update));
                Set<SyndEntry> entrySet = Sets.difference(update, repeats);
                for( SyndEntry item : entrySet) {
                    item.setSource(feed);
                    provider.inQueue.offer(item);
                }
                priorPollResult = update;
                Thread.sleep(feedDetails.getPollIntervalMillis());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

}
