package org.apache.streams.gnip.powertrack.test;

import org.apache.streams.gnip.powertrack.PowerTrackActivitySerializer;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created with IntelliJ IDEA.
 * User: rebanks
 * Date: 9/6/13
 * Time: 9:28 AM
 * To change this template use File | Settings | File Templates.
 */
public class PowerTrackDeserializationTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(PowerTrackActivitySerializer.class);

    private static final String ptData1= "{\"gnip\":{\"matching_rules\":[{\"tag\":\"toujours_DE_DE_de\"}],\"klout_score\":32,\"klout_profile\":{\"topics\":[],\"klout_user_id\":\"141018977628748348\",\"link\":\"http://klout.com/user/id/141018977628748348\"},\"language\":{\"value\":\"fr\"}},\"body\":\"RT @Albayraakkk: J'attends ton SMS, tu attends le miens. On croit toujours que l'autre va envoyer un message en premier. Bref, on ne se par\\u201a??\",\"favoritesCount\":0,\"link\":\"http://twitter.com/alexandragriett/statuses/375760903104638976\",\"retweetCount\":168,\"twitter_lang\":\"fr\",\"postedTime\":\"2013-09-05T23:22:35.000Z\",\"provider\":{\"link\":\"http://www.twitter.com\",\"displayName\":\"Twitter\",\"objectType\":\"service\"},\"actor\":{\"summary\":null,\"twitterTimeZone\":null,\"friendsCount\":68,\"favoritesCount\":3,\"link\":\"http://www.twitter.com/alexandragriett\",\"postedTime\":\"2013-05-01T17:52:16.000Z\",\"image\":\"https://si0.twimg.com/profile_images/378800000414353370/ef4170ca183eed9f7f30665712baba05_normal.jpeg\",\"links\":[{\"rel\":\"me\",\"href\":null}],\"listedCount\":0,\"id\":\"id:twitter.com:1395160326\",\"languages\":[\"fr\"],\"verified\":false,\"utcOffset\":null,\"followersCount\":47,\"preferredUsername\":\"alexandragriett\",\"displayName\":\"MauvaiseIdee\",\"statusesCount\":333,\"objectType\":\"person\"},\"object\":{\"body\":\"J'attends ton SMS, tu attends le miens. On croit toujours que l'autre va envoyer un message en premier. Bref, on ne se parle plus...\",\"favoritesCount\":24,\"link\":\"http://twitter.com/Albayraakkk/statuses/370496182172540928\",\"twitter_lang\":\"fr\",\"postedTime\":\"2013-08-22T10:42:27.000Z\",\"provider\":{\"link\":\"http://www.twitter.com\",\"displayName\":\"Twitter\",\"objectType\":\"service\"},\"actor\":{\"summary\":\"Le meilleur est dans mes favoris ! J'followback sur Instagram : http://instagram.com/Albayraakkk Kik : Cihan69200 #TeamBooba #TeamLacrim\",\"twitterTimeZone\":\"Athens\",\"friendsCount\":24998,\"favoritesCount\":677,\"location\":{\"displayName\":\"Dans Ta Timeline\",\"objectType\":\"place\"},\"link\":\"http://www.twitter.com/Albayraakkk\",\"postedTime\":\"2012-06-23T20:59:05.000Z\",\"image\":\"https://si0.twimg.com/profile_images/378800000410070574/26edc26ad5ccb223da8b850244b468eb_normal.jpeg\",\"links\":[{\"rel\":\"me\",\"href\":\"http://facebook.com/CihanAlbayraak\"}],\"listedCount\":69,\"id\":\"id:twitter.com:616472380\",\"languages\":[\"fr\"],\"verified\":false,\"utcOffset\":\"10800\",\"followersCount\":76068,\"preferredUsername\":\"Albayraakkk\",\"displayName\":\"LA VIRGULE \\u201a??\",\"statusesCount\":671,\"objectType\":\"person\"},\"object\":{\"summary\":\"J'attends ton SMS, tu attends le miens. On croit toujours que l'autre va envoyer un message en premier. Bref, on ne se parle plus...\",\"id\":\"object:search.twitter.com,2005:370496182172540928\",\"link\":\"http://twitter.com/Albayraakkk/statuses/370496182172540928\",\"postedTime\":\"2013-08-22T10:42:27.000Z\",\"objectType\":\"note\"},\"twitter_entities\":{\"symbols\":[],\"urls\":[],\"hashtags\":[],\"user_mentions\":[]},\"id\":\"tag:search.twitter.com,2005:370496182172540928\",\"verb\":\"post\",\"generator\":{\"link\":\"http://twitter.com/download/iphone\",\"displayName\":\"Twitter for iPhone\"},\"objectType\":\"activity\"},\"twitter_entities\":{\"symbols\":[],\"urls\":[],\"hashtags\":[],\"user_mentions\":[{\"id\":616472380,\"name\":\"LA VIRGULE \\u201a??\",\"indices\":[3,15],\"screen_name\":\"Albayraakkk\",\"id_str\":\"616472380\"}]},\"twitter_filter_level\":\"medium\",\"content\":\"RT @Albayraakkk: J'attends ton SMS, tu attends le miens. On croit toujours que l'autre va envoyer un message en premier. Bref, on ne se par\\u201a??\",\"id\":\"tag:search.twitter.com,2005:375760903104638976\",\"verb\":\"share\",\"generator\":{\"link\":\"http://twitter.com/download/iphone\",\"displayName\":\"Twitter for iPhone\"},\"published\":\"2013-09-05T23:22:35.000Z\",\"objectType\":\"activity\"}";

    @Test
    public void deserializationTest() {

        PowerTrackActivitySerializer serializer = new PowerTrackActivitySerializer();
        try {
        Object activity = serializer.deserialize(ptData1);
        } catch( Exception e ) {
            LOGGER.error(e.getMessage(), e );
            e.printStackTrace();
            Assert.fail();
        }
    }



}
