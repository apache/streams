package org.apache.streams.twitter.provider;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.base.Preconditions;
import org.apache.commons.lang.StringUtils;
import org.apache.streams.twitter.pojo.*;
import org.apache.streams.twitter.serializer.StreamsTwitterMapper;

import java.io.IOException;

/**
 * Created by sblackmon on 12/13/13.
 */
public class TwitterEventClassifier {

    public static Class detectClass( String json ) {

        Preconditions.checkNotNull(json);
        Preconditions.checkArgument(StringUtils.isNotEmpty(json));

//        try {
//            JsonAssert.with(json).assertNull("$.delete");
//        } catch( AssertionError ae ) {
//            return Delete.class;
//        }
//
//        try {
//            JsonAssert.with(json).assertNull("$.retweeted_status");
//        } catch( AssertionError ae ) {
//            return Retweet.class;
//        }
//
//        return Tweet.class;

        ObjectNode objectNode;
        try {
            objectNode = (ObjectNode) StreamsTwitterMapper.getInstance().readTree(json);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

        if( objectNode.findValue("retweeted_status") != null )
            return Retweet.class;
        else if( objectNode.findValue("delete") != null )
            return Delete.class;
        else if( objectNode.findValue("friends") != null ||
                 objectNode.findValue("friends_str") != null )
            return FriendList.class;
        else if( objectNode.findValue("target_object") != null )
            return UserstreamEvent.class;
        else
            return Tweet.class;
    }
}
