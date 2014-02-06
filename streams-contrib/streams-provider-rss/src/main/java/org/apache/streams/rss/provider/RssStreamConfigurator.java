package org.apache.streams.rss.provider;

import com.google.common.collect.Lists;
import com.typesafe.config.Config;
import org.apache.streams.rss.FeedDetails;
import org.apache.streams.rss.RssStreamConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Created by sblackmon on 12/10/13.
 */
public class RssStreamConfigurator {

    private final static Logger LOGGER = LoggerFactory.getLogger(RssStreamConfigurator.class);

    public static RssStreamConfiguration detectConfiguration(Config rss) {

        RssStreamConfiguration rssStreamConfiguration = new RssStreamConfiguration();

        List<FeedDetails> feeds = Lists.newArrayList();
        feeds.add(new FeedDetails().withUrl(rss.getString("url")));

        rssStreamConfiguration.setFeeds(feeds);
        return rssStreamConfiguration;
    }

}
