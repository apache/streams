package org.apache.streams.twitter.provider;

import com.jayway.jsonassert.JsonAssert;
import org.apache.streams.twitter.pojo.Delete;
import org.apache.streams.twitter.pojo.Retweet;
import org.apache.streams.twitter.pojo.Tweet;

/**
 * Created by sblackmon on 12/13/13.
 */
public class TwitterEventClassifier {

    public static Class detectClass( String json ) {

        try {
            JsonAssert.with(json).assertNull("$.delete");
        } catch( AssertionError ae ) {
            return Delete.class;
        }

        try {
            JsonAssert.with(json).assertNull("$.retweeted_status");
        } catch( AssertionError ae ) {
            return Retweet.class;
        }

        return Tweet.class;
    }
}
