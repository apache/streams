package org.apache.streams.rss.provider;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndEntryImpl;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.feed.synd.SyndFeedImpl;
import org.apache.streams.threaded.builders.ThreadedStreamBuilder;
import org.junit.Test;

import java.net.URL;
import java.util.Set;

import static org.junit.Assert.assertEquals;

public class RssLinkProviderTest {

    @Test
    public void testProvider() {
        ThreadedStreamBuilder builder = new ThreadedStreamBuilder();
        Set<String> feeds = Sets.newHashSet();

        feeds.add("http://google.com");
        feeds.add("http://testFeed2.com");
        feeds.add("http://testFeed3.com");

        RssLinkTestProvider provider = new RssLinkTestProvider(feeds);
        CollectorProcessor processor = new CollectorProcessor();

        builder.newPerpetualStream("rssProvider", provider)
                .addStreamsProcessor("collectorProcessor", processor, 1, "rssProvider");

        builder.start();

        assertEquals("Did not get correct number of url entries.", processor.getDocCount(), feeds.size());
    }

    private class RssLinkTestProvider extends RssLinkProvider {

        public RssLinkTestProvider(Set<String> feeds) {
            super(feeds);
        }

        @Override
        protected SyndFeed buildFeed(URL feedUrl) {
            SyndFeed entry = new SyndFeedImpl();
            SyndEntry obj = new SyndEntryImpl();
            obj.setUri(feedUrl.toString());

            entry.setEntries(Lists.newArrayList(obj));

            return entry;
        }
    }
}