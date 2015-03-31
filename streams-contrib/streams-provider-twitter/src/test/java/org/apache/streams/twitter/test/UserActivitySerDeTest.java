package org.apache.streams.twitter.test;

import org.apache.streams.pojo.json.Activity;
import org.apache.streams.pojo.json.Actor;
import org.apache.streams.twitter.serializer.TwitterJsonActivitySerializer;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class UserActivitySerDeTest {
    private TwitterJsonActivitySerializer twitterJsonActivitySerializer = new TwitterJsonActivitySerializer();

    private final static Logger LOGGER = LoggerFactory.getLogger(UserActivitySerDeTest.class);
    private final String user = "{\"location\":\"Washington DC\",\"default_profile\":true,\"statuses_count\":166,\"profile_background_tile\":false,\"lang\":\"en\",\"profile_link_color\":\"0084B4\",\"entities\":{\"description\":{},\"url\":{\"urls\":[{\"expanded_url\":\"https://www.facebook.com/pages/Anjali-S-Kumar-MD-MPH-FACS/413609345448744\",\"indices\":[0,23],\"display_url\":\"facebook.com/pages/Anjali-S…\",\"url\":\"https://t.co/pfy776iXn3\"}]}},\"id\":2553618199,\"protected\":false,\"favourites_count\":1,\"profile_text_color\":\"333333\",\"verified\":false,\"description\":\"Colon and rectal surgeon, traveler, teacher, outdoorswoman, UW alum, mother to a mighty toddler. I ♥ my patients.\",\"contributors_enabled\":false,\"name\":\"Anjali Kumar MD MPH\",\"profile_sidebar_border_color\":\"C0DEED\",\"profile_background_color\":\"C0DEED\",\"created_at\":\"2014-06-08T01:06:37.000Z\",\"default_profile_image\":false,\"followers_count\":186,\"geo_enabled\":false,\"profile_image_url_https\":\"https://pbs.twimg.com/profile_images/475445736864104448/I8Bu8HZE_normal.jpeg\",\"profile_background_image_url\":\"http://abs.twimg.com/images/themes/theme1/bg.png\",\"profile_background_image_url_https\":\"https://abs.twimg.com/images/themes/theme1/bg.png\",\"follow_request_sent\":false,\"url\":\"https://t.co/pfy776iXn3\",\"profile_use_background_image\":true,\"friends_count\":152,\"profile_sidebar_fill_color\":\"DDEEF6\",\"screen_name\":\"anjaliskumar_md\",\"id_str\":\"2553618199\",\"profile_image_url\":\"http://pbs.twimg.com/profile_images/475445736864104448/I8Bu8HZE_normal.jpeg\",\"is_translator\":false,\"listed_count\":3,\"following\":false,\"profile_location\":null,\"notifications\":false,\"status\":{\"text\":\"MiniMe at an Easter Egg hunt in sunny Houston TX before boarding our flight back to freezing cold DC after #SSO2015 http://t.co/uAR1TRIsS8\",\"retweeted\":false,\"possibly_sensitive\":false,\"truncated\":false,\"lang\":\"en\",\"entities\":{\"symbols\":[],\"urls\":[],\"hashtags\":[{\"text\":\"SSO2015\",\"indices\":[107,115]}],\"media\":[{\"sizes\":{\"thumb\":{\"w\":150,\"resize\":\"crop\",\"h\":150},\"small\":{\"w\":340,\"resize\":\"fit\",\"h\":255},\"medium\":{\"w\":600,\"resize\":\"fit\",\"h\":450},\"large\":{\"w\":640,\"resize\":\"fit\",\"h\":480}},\"id\":582256760833994752,\"media_url_https\":\"https://pbs.twimg.com/media/CBSXcGWUcAA2Nzw.jpg\",\"media_url\":\"http://pbs.twimg.com/media/CBSXcGWUcAA2Nzw.jpg\",\"expanded_url\":\"http://twitter.com/anjaliskumar_md/status/582256761001791488/photo/1\",\"indices\":[116,138],\"id_str\":\"582256760833994752\",\"type\":\"photo\",\"display_url\":\"pic.twitter.com/uAR1TRIsS8\",\"url\":\"http://t.co/uAR1TRIsS8\"}],\"user_mentions\":[]},\"id\":582256761001791488,\"source\":\"<a href=\\\"http://blackberry.com/twitter\\\" rel=\\\"nofollow\\\">Twitter for BlackBerry®</a>\",\"favorited\":false,\"retweet_count\":0,\"created_at\":\"Sun Mar 29 19:03:24 +0000 2015\",\"favorite_count\":0,\"id_str\":\"582256761001791488\"},\"is_translation_enabled\":false,\"profile_banner_url\":\"https://pbs.twimg.com/profile_banners/2553618199/1402191182\"}";

    @Test
    public void testUserSerDe() {
        try {
            Activity activity = twitterJsonActivitySerializer.deserialize(user);

            assertNotNull(activity);

            Actor actor = activity.getActor();
            assertTrue(actor.getId().contains("id:twitter:"));
            assertNotNull(actor.getDisplayName());
            assertNotNull(actor.getSummary());
            assertNotNull(actor.getUrl());

            assertEquals("post", activity.getVerb());
            assertNotNull(activity.getPublished());
        } catch (Exception e) {
            LOGGER.error("Exception while testing Twitter User object deserialization: {}", e);
        }
    }
}
