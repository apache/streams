package org.apache.streams.twitter.test;

/*
 * #%L
 * streams-provider-twitter
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

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.commons.lang.StringUtils;
import org.apache.streams.pojo.json.Activity;
import org.apache.streams.twitter.pojo.Retweet;
import org.apache.streams.twitter.pojo.Tweet;
import org.apache.streams.twitter.provider.TwitterEventClassifier;
import org.apache.streams.twitter.serializer.StreamsTwitterMapper;
import org.apache.streams.twitter.serializer.TwitterJsonActivitySerializer;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

import static java.util.regex.Pattern.matches;
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
public class TweetActivitySerDeTest {

    private final static Logger LOGGER = LoggerFactory.getLogger(TweetActivitySerDeTest.class);
    private ObjectMapper mapper = StreamsTwitterMapper.getInstance();

    private TwitterJsonActivitySerializer twitterJsonActivitySerializer = new TwitterJsonActivitySerializer();

    //    @Ignore
    @Test
    public void Tests()
    {
        InputStream is = TweetActivitySerDeTest.class.getResourceAsStream("/testtweets.txt");
        InputStreamReader isr = new InputStreamReader(is);
        BufferedReader br = new BufferedReader(isr);

        try {
            while (br.ready()) {
                String line = br.readLine();
                if(!StringUtils.isEmpty(line))
                {
                    LOGGER.info("raw: {}", line);

                    Class detected = TwitterEventClassifier.detectClass(line);

                    Activity activity = twitterJsonActivitySerializer.deserialize(line);

                    String activitystring = mapper.writeValueAsString(activity);

                    LOGGER.info("activity: {}", activitystring);

                    assertThat(activity, is(not(nullValue())));

                    assertThat(activity.getId(), is(not(nullValue())));
                    assertThat(activity.getActor(), is(not(nullValue())));
                    assertThat(activity.getActor().getId(), is(not(nullValue())));
                    assertThat(activity.getVerb(), is(not(nullValue())));
                    assertThat(activity.getProvider(), is(not(nullValue())));

                    if( detected == Tweet.class ) {
                        assertThat(activity.getObject(), is(nullValue()));

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
