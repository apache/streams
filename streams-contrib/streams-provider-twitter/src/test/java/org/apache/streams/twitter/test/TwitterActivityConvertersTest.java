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

package org.apache.streams.twitter.test;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang.StringUtils;
import org.apache.streams.converter.TypeConverterUtil;
import org.apache.streams.data.ActivityConverter;
import org.apache.streams.jackson.StreamsJacksonMapper;
import org.apache.streams.pojo.json.Activity;
import org.apache.streams.twitter.pojo.Retweet;
import org.apache.streams.twitter.pojo.Tweet;
import org.apache.streams.twitter.converter.TwitterDocumentClassifier;
import org.apache.streams.twitter.converter.StreamsTwitterMapper;
import org.apache.streams.twitter.converter.TwitterJsonRetweetActivityConverter;
import org.apache.streams.twitter.converter.TwitterJsonTweetActivityConverter;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

/**
* Created with IntelliJ IDEA.
* User: sblackmon
* Date: 8/20/13
* Time: 5:57 PM
* To change this template use File | Settings | File Templates.
*/
public class TwitterActivityConvertersTest {

    private final static Logger LOGGER = LoggerFactory.getLogger(TwitterActivityConvertersTest.class);

    private ObjectMapper mapper = StreamsJacksonMapper.getInstance(StreamsTwitterMapper.TWITTER_FORMAT);

    @Test
    public void Tests()
    {
        InputStream is = TwitterActivityConvertersTest.class.getResourceAsStream("/testtweets.txt");
        InputStreamReader isr = new InputStreamReader(is);
        BufferedReader br = new BufferedReader(isr);

        ActivityConverter activityConverter;

        try {
            while (br.ready()) {
                String line = br.readLine();
                if(!StringUtils.isEmpty(line))
                {
                    LOGGER.info("raw: {}", line);

                    Class detected = new TwitterDocumentClassifier().detectClasses(line).get(0);

                    if( detected == Tweet.class ) {
                        activityConverter = new TwitterJsonTweetActivityConverter();
                    } else if( detected == Retweet.class ) {
                        activityConverter = new TwitterJsonRetweetActivityConverter();
                    } else {
                        Assert.fail();
                        return;
                    }

                    Object typedObject = TypeConverterUtil.convert(line, detected, mapper);

                    Activity activity = (Activity) activityConverter.toActivityList(typedObject).get(0);

                    String activitystring = mapper.writeValueAsString(activity);

                    LOGGER.info("activity: {}", activitystring);

                    assertThat(activity, is(not(nullValue())));

                    assertThat(activity.getId(), is(not(nullValue())));
                    assertThat(activity.getActor(), is(not(nullValue())));
                    assertThat(activity.getActor().getId(), is(not(nullValue())));
                    assertThat(activity.getVerb(), is(not(nullValue())));
                    assertThat(activity.getProvider(), is(not(nullValue())));

                    if( detected == Tweet.class ) {

                        assertEquals(activity.getVerb(), "post");

                        Tweet tweet = mapper.readValue(line, Tweet.class);

                        if( tweet.getEntities() != null &&
                            tweet.getEntities().getUrls() != null &&
                            tweet.getEntities().getUrls().size() > 0 ) {


                            assertThat(activity.getLinks(), is(not(nullValue())));
                            assertEquals(tweet.getEntities().getUrls().size(), activity.getLinks().size());
                        }

                    } else if( detected == Retweet.class ) {

                        Retweet retweet = mapper.readValue(line, Retweet.class);

                        assertThat(retweet.getRetweetedStatus(), is(not(nullValue())));

                        assertEquals(activity.getVerb(), "share");

                        assertThat(activity.getObject(), is(not(nullValue())));
                        assertThat(activity.getObject().getObjectType(), is(not(nullValue())));
                        assertThat(activity.getObject().getObjectType(), is(not(nullValue())));

                        if( retweet.getRetweetedStatus().getEntities() != null &&
                            retweet.getRetweetedStatus().getEntities().getUrls() != null &&
                            retweet.getRetweetedStatus().getEntities().getUrls().size() > 0 ) {

                            assertThat(activity.getLinks(), is(not(nullValue())));
                            assertEquals(retweet.getRetweetedStatus().getEntities().getUrls().size(), activity.getLinks().size());
                        }

                    }



                }
            }
        } catch( Exception e ) {
            System.out.println(e);
            e.printStackTrace();
            Assert.fail();
        }
    }
}
