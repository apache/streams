package org.apache.streams.twitter.test;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.commons.lang.StringUtils;
import org.apache.streams.pojo.json.Activity;
import org.apache.streams.twitter.pojo.Delete;
import org.apache.streams.twitter.pojo.Retweet;
import org.apache.streams.twitter.pojo.Tweet;
import org.apache.streams.twitter.provider.TwitterEventClassifier;
import org.apache.streams.twitter.serializer.TwitterJsonDeleteActivitySerializer;
import org.apache.streams.twitter.serializer.TwitterJsonRetweetActivitySerializer;
import org.apache.streams.twitter.serializer.TwitterJsonTweetActivitySerializer;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

import static java.util.regex.Pattern.matches;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;

/**
* Created with IntelliJ IDEA.
* User: sblackmon
* Date: 8/20/13
* Time: 5:57 PM
* To change this template use File | Settings | File Templates.
*/
public class TweetSerDeTest {

    private final static Logger LOGGER = LoggerFactory.getLogger(TweetSerDeTest.class);
    private ObjectMapper mapper = new ObjectMapper();

    private TwitterJsonTweetActivitySerializer twitterJsonTweetActivitySerializer = new TwitterJsonTweetActivitySerializer();
    private TwitterJsonRetweetActivitySerializer twitterJsonRetweetActivitySerializer = new TwitterJsonRetweetActivitySerializer();
    private TwitterJsonDeleteActivitySerializer twitterJsonDeleteActivitySerializer = new TwitterJsonDeleteActivitySerializer();

    //    @Ignore
    @Test
    public void Tests()
    {
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, Boolean.FALSE);
        mapper.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, Boolean.TRUE);
        mapper.configure(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT, Boolean.TRUE);

        InputStream is = TweetSerDeTest.class.getResourceAsStream("/twitter_jsons.txt");
        InputStreamReader isr = new InputStreamReader(is);
        BufferedReader br = new BufferedReader(isr);

        try {
            while (br.ready()) {
                String line = br.readLine();
                if(!StringUtils.isEmpty(line))
                {
                    LOGGER.info("raw: {}", line);

                    Class detected = TwitterEventClassifier.detectClass(line);

                    ObjectNode event = (ObjectNode) mapper.readTree(line);

                    assertThat(event, is(not(nullValue())));

                    String tweetstring = mapper.writeValueAsString(event);

                    LOGGER.info("{}: {}", detected.getName(), tweetstring);

                    Activity activity;
                    if( detected.equals( Delete.class )) {
                        activity = twitterJsonDeleteActivitySerializer.convert(event);
                    } else if ( detected.equals( Retweet.class )) {
                        activity = twitterJsonRetweetActivitySerializer.convert(event);
                    } else if ( detected.equals( Tweet.class )) {
                        activity = twitterJsonTweetActivitySerializer.convert(event);
                    } else {
                        Assert.fail();
                        return;
                    }

                    String activitystring = mapper.writeValueAsString(activity);

                    LOGGER.info("activity: {}", activitystring);

                    assertThat(activity, is(not(nullValue())));
                    if(activity.getId() != null) {
                        assertThat(matches("id:.*:[a-z]*:[a-zA-Z0-9]*", activity.getId()), is(true));
                    }
                    assertThat(activity.getActor(), is(not(nullValue())));
                    assertThat(activity.getActor().getId(), is(not(nullValue())));
                    assertThat(activity.getVerb(), is(not(nullValue())));
                    assertThat(activity.getObject(), is(not(nullValue())));
                    assertThat(activity.getObject().getObjectType(), is(not(nullValue())));
                }
            }
        } catch( Exception e ) {
            System.out.println(e);
            e.printStackTrace();
            Assert.fail();
        }
    }
}
