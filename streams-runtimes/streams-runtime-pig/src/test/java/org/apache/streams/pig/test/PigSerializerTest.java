package org.apache.streams.pig.test;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import org.apache.pig.data.Tuple;
import org.apache.pig.pigunit.PigTest;
import org.apache.streams.jackson.StreamsJacksonMapper;
import org.apache.streams.twitter.serializer.TwitterJsonActivitySerializer;
import org.apache.streams.twitter.serializer.TwitterJsonTweetActivitySerializer;
import org.apache.tools.ant.util.StringUtils;
import org.junit.Ignore;
import org.junit.Test;

import java.io.File;
import java.util.Iterator;

/**
 * Created by sblackmon on 3/30/14.
 */
public class PigSerializerTest {

    @Test
    public void testPigSerializer() throws Exception {

        String[] input = {
                "159475541894897679\ttwitter,statuses/user_timeline\t1384499359006\t{\"retweeted_status\":{\"contributors\":null,\"text\":\"The Costa Concordia cruise ship accident could be a disaster for the industry | http://t.co/M9UUNvZi (via @TIMEMoneyland)\",\"geo\":null,\"retweeted\":false,\"in_reply_to_screen_name\":null,\"possibly_sensitive\":false,\"truncated\":false,\"lang\":\"en\",\"entities\":{\"symbols\":[],\"urls\":[{\"expanded_url\":\"http://ti.me/zYyEtD\",\"indices\":[80,100],\"display_url\":\"ti.me/zYyEtD\",\"url\":\"http://t.co/M9UUNvZi\"}],\"hashtags\":[],\"user_mentions\":[{\"id\":245888431,\"name\":\"TIME Moneyland\",\"indices\":[106,120],\"screen_name\":\"TIMEMoneyland\",\"id_str\":\"245888431\"}]},\"in_reply_to_status_id_str\":null,\"id\":159470076259602432,\"source\":\"<a href=\\\"http://www.hootsuite.com\\\" rel=\\\"nofollow\\\">HootSuite<\\/a>\",\"in_reply_to_user_id_str\":null,\"favorited\":false,\"in_reply_to_status_id\":null,\"retweet_count\":71,\"created_at\":\"Wed Jan 18 03:00:03 +0000 2012\",\"in_reply_to_user_id\":null,\"favorite_count\":14,\"id_str\":\"159470076259602432\",\"place\":null,\"user\":{\"location\":\"\",\"default_profile\":false,\"profile_background_tile\":true,\"statuses_count\":70754,\"lang\":\"en\",\"profile_link_color\":\"1B4F89\",\"profile_banner_url\":\"https://pbs.twimg.com/profile_banners/14293310/1355243462\",\"id\":14293310,\"following\":false,\"protected\":false,\"favourites_count\":59,\"profile_text_color\":\"000000\",\"description\":\"Breaking news and current events from around the globe. Hosted by TIME staff. Tweet questions to our customer service team @TIMEmag_Service.\",\"verified\":true,\"contributors_enabled\":false,\"profile_sidebar_border_color\":\"000000\",\"name\":\"TIME.com\",\"profile_background_color\":\"CC0000\",\"created_at\":\"Thu Apr 03 13:54:30 +0000 2008\",\"default_profile_image\":false,\"followers_count\":5146268,\"profile_image_url_https\":\"https://pbs.twimg.com/profile_images/1700796190/Picture_24_normal.png\",\"geo_enabled\":false,\"profile_background_image_url\":\"http://a0.twimg.com/profile_background_images/735228291/107f1a300a90ee713937234bb3d139c0.jpeg\",\"profile_background_image_url_https\":\"https://si0.twimg.com/profile_background_images/735228291/107f1a300a90ee713937234bb3d139c0.jpeg\",\"follow_request_sent\":false,\"entities\":{\"description\":{\"urls\":[]},\"url\":{\"urls\":[{\"expanded_url\":\"http://www.time.com\",\"indices\":[0,22],\"display_url\":\"time.com\",\"url\":\"http://t.co/4aYbUuAeSh\"}]}},\"url\":\"http://t.co/4aYbUuAeSh\",\"utc_offset\":-18000,\"time_zone\":\"Eastern Time (US & Canada)\",\"notifications\":false,\"profile_use_background_image\":true,\"friends_count\":742,\"profile_sidebar_fill_color\":\"D9D9D9\",\"screen_name\":\"TIME\",\"id_str\":\"14293310\",\"profile_image_url\":\"http://pbs.twimg.com/profile_images/1700796190/Picture_24_normal.png\",\"listed_count\":76944,\"is_translator\":false},\"coordinates\":null},\"contributors\":null,\"text\":\"RT @TIME: The Costa Concordia cruise ship accident could be a disaster for the industry | http://t.co/M9UUNvZi (via @TIMEMoneyland)\",\"geo\":null,\"retweeted\":false,\"in_reply_to_screen_name\":null,\"possibly_sensitive\":false,\"truncated\":false,\"lang\":\"en\",\"entities\":{\"symbols\":[],\"urls\":[{\"expanded_url\":\"http://ti.me/zYyEtD\",\"indices\":[90,110],\"display_url\":\"ti.me/zYyEtD\",\"url\":\"http://t.co/M9UUNvZi\"}],\"hashtags\":[],\"user_mentions\":[{\"id\":14293310,\"name\":\"TIME.com\",\"indices\":[3,8],\"screen_name\":\"TIME\",\"id_str\":\"14293310\"},{\"id\":245888431,\"name\":\"TIME Moneyland\",\"indices\":[116,130],\"screen_name\":\"TIMEMoneyland\",\"id_str\":\"245888431\"}]},\"in_reply_to_status_id_str\":null,\"id\":159475541894897679,\"source\":\"<a href=\\\"http://twitter.com/download/iphone\\\" rel=\\\"nofollow\\\">Twitter for iPhone<\\/a>\",\"in_reply_to_user_id_str\":null,\"favorited\":false,\"in_reply_to_status_id\":null,\"retweet_count\":71,\"created_at\":\"Wed Jan 18 03:21:46 +0000 2012\",\"in_reply_to_user_id\":null,\"favorite_count\":0,\"id_str\":\"159475541894897679\",\"place\":null,\"user\":{\"location\":\"\",\"default_profile\":false,\"profile_background_tile\":true,\"statuses_count\":5053,\"lang\":\"en\",\"profile_link_color\":\"738D84\",\"id\":27552112,\"following\":false,\"protected\":false,\"favourites_count\":52,\"profile_text_color\":\"97CEC9\",\"description\":\"\",\"verified\":false,\"contributors_enabled\":false,\"profile_sidebar_border_color\":\"A9AC00\",\"name\":\"rafael medina-flores\",\"profile_background_color\":\"C5EFE3\",\"created_at\":\"Mon Mar 30 01:21:55 +0000 2009\",\"default_profile_image\":false,\"followers_count\":963,\"profile_image_url_https\":\"https://pbs.twimg.com/profile_images/2519547938/image_normal.jpg\",\"geo_enabled\":true,\"profile_background_image_url\":\"http://a0.twimg.com/profile_background_images/167479660/trireme.jpg\",\"profile_background_image_url_https\":\"https://si0.twimg.com/profile_background_images/167479660/trireme.jpg\",\"follow_request_sent\":false,\"entities\":{\"description\":{\"urls\":[]}},\"url\":null,\"utc_offset\":-25200,\"time_zone\":\"Mountain Time (US & Canada)\",\"notifications\":false,\"profile_use_background_image\":true,\"friends_count\":1800,\"profile_sidebar_fill_color\":\"5C4F3C\",\"screen_name\":\"rmedinaflores\",\"id_str\":\"27552112\",\"profile_image_url\":\"http://pbs.twimg.com/profile_images/2519547938/image_normal.jpg\",\"listed_count\":50,\"is_translator\":false},\"coordinates\":null}"
        };

        TwitterJsonActivitySerializer serializer = new TwitterJsonActivitySerializer();

        String doc = (String) StringUtils.split(input[0], '\t').get(3);
        String outdoc = StreamsJacksonMapper.getInstance().writeValueAsString(serializer.deserialize(doc));

        String[] output = new String[1];
        output[0] = "(159475541894897679,twitter,statuses/user_timeline,1384499359006," + outdoc + ")";

        PigTest test;
        test = new PigTest("src/test/resources/pigserializertest.pig");
        test.assertOutput("in", input, "out", output);

    }
}
