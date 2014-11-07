package org.apache.streams.datasift.serializer;

import org.apache.streams.data.ActivitySerializer;
import org.apache.streams.datasift.Datasift;
import org.apache.streams.datasift.instagram.Instagram;
import org.apache.streams.datasift.interaction.Interaction;
import org.apache.streams.datasift.twitter.Twitter;

/**
 * Created by sblackmon on 11/6/14.
 */
public class DatasiftEventClassifier {

    public static Class detectClass(Datasift event) {

        if(event.getTwitter() != null) {
            return Twitter.class;
        } else if(event.getInstagram() != null) {
            return Instagram.class;
        } else {
            return Interaction.class;
        }
    }

    public static ActivitySerializer bestSerializer(Datasift event) {

        if(event.getTwitter() != null) {
            return DatasiftTweetActivitySerializer.getInstance();
        } else if(event.getInstagram() != null) {
            return DatasiftInstagramActivitySerializer.getInstance();
        } else {
            return DatasiftInteractionActivitySerializer.getInstance();
        }
    }
}
