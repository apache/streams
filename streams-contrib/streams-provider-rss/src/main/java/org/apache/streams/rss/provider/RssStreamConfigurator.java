package org.apache.streams.rss.provider;

/*
 * #%L
 * streams-provider-rss
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
