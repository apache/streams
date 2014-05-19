package org.apache.streams.rss.test;

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
import org.apache.commons.lang.StringUtils;
import org.apache.streams.pojo.json.Activity;
import org.apache.streams.rss.FeedDetails;
import org.apache.streams.rss.RssStreamConfiguration;
import org.apache.streams.rss.provider.RssStreamProvider;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

import static org.hamcrest.number.OrderingComparison.greaterThan;

/**
 * Created by sblackmon on 2/5/14.
 */
public class Top100FeedsTest{

    private final static Logger LOGGER = LoggerFactory.getLogger(Top100FeedsTest.class);

    @Test
    public void Tests()
    {
        InputStream is = Top100FeedsTest.class.getResourceAsStream("/top100.txt");
        InputStreamReader isr = new InputStreamReader(is);
        BufferedReader br = new BufferedReader(isr);

        RssStreamConfiguration configuration = new RssStreamConfiguration();
        List<FeedDetails> feeds = Lists.newArrayList();
        try {
            while (br.ready()) {
                String line = br.readLine();
                if(!StringUtils.isEmpty(line))
                {
                    feeds.add(new FeedDetails().withUrl(line).withPollIntervalMillis(5000l));
                }
            }
        } catch( Exception e ) {
            System.out.println(e);
            e.printStackTrace();
            Assert.fail();
        }

        Assert.assertThat(feeds.size(), greaterThan(70));

        configuration.setFeeds(feeds);

        RssStreamProvider provider = new RssStreamProvider(configuration, Activity.class);
        provider.prepare(configuration);
        provider.startStream();

        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {}

        Assert.assertThat(provider.getProviderQueue().size(), greaterThan(0));

    }
}
