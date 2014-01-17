package org.apache.streams.twitter.test;

import com.google.common.collect.Lists;
import org.apache.streams.twitter.TwitterOAuthConfiguration;
import org.apache.streams.twitter.TwitterStreamConfiguration;
import org.apache.streams.twitter.provider.TwitterStreamProvider;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by sblackmon on 12/10/13.
 */
public class TwitterStreamProviderTest {

    private final static Logger LOGGER = LoggerFactory.getLogger(TwitterStreamProviderTest.class);

    @Ignore
    @Test
    public void Tests()
    {
        TwitterStreamConfiguration twitterStreamConfiguration = new TwitterStreamConfiguration();
        TwitterOAuthConfiguration twitterOAuthConfiguration = new TwitterOAuthConfiguration();
        twitterOAuthConfiguration.setAccessToken("281592383-DMabF7UmiZqDAyzHwNPe09iruBSplrt9nHdavZP4");
        twitterOAuthConfiguration.setAccessTokenSecret("uA1oJcSEkWB9gAchE3J1FsCZlagxgunVRmfXx62OZU");
        twitterStreamConfiguration.setOauth(twitterOAuthConfiguration);
        twitterStreamConfiguration.setTrack(Lists.newArrayList("stream"));

        TwitterStreamProvider stream = new TwitterStreamProvider(twitterStreamConfiguration);

        //Assert.assertEquals(stream.getUrl().substring(0,5), "https");
        //Assert.assertNotNull(stream.getParams().get("track"));
        //Assert.assertFalse(stream.getParams().containsKey("follow"));
        //Assert.assertArrayEquals(Lists.newArrayList("track").toArray(), Lists.newArrayList(stream.getParams().keySet()).toArray());

        // any deterministic test of the stream would go here

    }
}
