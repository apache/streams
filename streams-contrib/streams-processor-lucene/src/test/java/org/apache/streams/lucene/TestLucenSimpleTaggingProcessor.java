package org.apache.streams.lucene;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.jayway.jsonpath.JsonPath;
import com.w2olabs.streams.pojo.W2OActivity;
import com.w2olabs.util.tagging.engines.international.LanguageTag;
import org.apache.streams.core.StreamsDatum;
import org.apache.streams.pojo.json.Activity;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

import java.util.List;
import java.util.Scanner;

/**
 * Created by rebanks on 3/18/14.
 */
public class TestLucenSimpleTaggingProcessor {

    private static final String ACTIVITY_JSON = "{\"id\":\"id:twitter:post:410898682381615105\",\"actor\":{\"id\":\"91407775\",\"displayName\":\"winchester_ky\",\"attachments\":[],\"upstreamDuplicates\":[],\"downstreamDuplicates\":[]},\"verb\":\"post\",\"object\":{\"id\":\"id:twitter:410898682381615105\",\"objectType\":\"tweet\",\"attachments\":[],\"upstreamDuplicates\":[],\"downstreamDuplicates\":[]},\"published\":1386800854000,\"provider\":{\"id\":\"id:providers:twitter\",\"attachments\":[],\"upstreamDuplicates\":[],\"downstreamDuplicates\":[]},\"title\":\"\",\"content\":\"Men's Basketball baseball soccer Single-Game Tickets Available - A limited number of tickets remain for Kentucky's upcoming men's ... http://t.co/SH5YZGpdRx\",\"links\":[\"http://ow.ly/2C2XL1\"],\"extensions\":{\"twitter\":{\"created_at\":\"Wed Dec 11 22:27:34 +0000 2013\",\"id\":410898682381615105,\"id_str\":\"410898682381615105\",\"text\":\"Men's Basketball Single-Game Tickets Available - A limited number of tickets remain for Kentucky's upcoming men's ... http://t.co/SH5YZGpdRx\",\"source\":\"<a href=\\\"http://www.hootsuite.com\\\" rel=\\\"nofollow\\\">HootSuite</a>\",\"truncated\":false,\"in_reply_to_status_id\":null,\"in_reply_to_status_id_str\":null,\"in_reply_to_user_id\":null,\"in_reply_to_user_id_str\":null,\"in_reply_to_screen_name\":null,\"user\":{\"id\":91407775,\"id_str\":\"91407775\",\"name\":\"Winchester, KY\",\"screen_name\":\"winchester_ky\",\"location\":\"\",\"urls\":null,\"description\":null,\"protected\":false,\"followers_count\":136,\"friends_count\":0,\"listed_count\":1,\"created_at\":\"Fri Nov 20 19:29:02 +0000 2009\",\"favourites_count\":0,\"utc_offset\":null,\"time_zone\":null,\"geo_enabled\":false,\"verified\":false,\"statuses_count\":1793,\"lang\":\"en\",\"contributors_enabled\":false,\"is_translator\":false,\"profile_background_color\":\"C0DEED\",\"profile_background_image_url\":\"http://abs.twimg.com/images/themes/theme1/bg.png\",\"profile_background_image_url_https\":\"https://abs.twimg.com/images/themes/theme1/bg.png\",\"profile_background_tile\":false,\"profile_image_url\":\"http://pbs.twimg.com/profile_images/613854495/winchester_sociallogo_normal.jpg\",\"profile_image_url_https\":\"https://pbs.twimg.com/profile_images/613854495/winchester_sociallogo_normal.jpg\",\"profile_link_color\":\"0084B4\",\"profile_sidebar_border_color\":\"C0DEED\",\"profile_sidebar_fill_color\":\"DDEEF6\",\"profile_text_color\":\"333333\",\"profile_use_background_image\":true,\"default_profile\":true,\"default_profile_image\":false,\"following\":null,\"follow_request_sent\":null,\"notifications\":null},\"geo\":null,\"coordinates\":null,\"place\":null,\"contributors\":null,\"retweet_count\":0,\"favorite_count\":0,\"entities\":{\"hashtags\":[],\"symbols\":[],\"urls\":[{\"urls\":\"http://t.co/SH5YZGpdRx\",\"expanded_url\":\"http://ow.ly/2C2XL1\",\"display_url\":\"ow.ly/2C2XL1\",\"indices\":[118,140]}],\"user_mentions\":[]},\"favorited\":false,\"retweeted\":false,\"possibly_sensitive\":false,\"filter_level\":\"medium\",\"lang\":\"en\"},\"location\":{\"id\":\"id:twitter:410898682381615105\",\"coordinates\":null}}}\n";
    private static final String W2O_ACTIVITY_JSON = "{\"extensions\":{\"w2o\":{\"provider\":\"twitter\",\"analyzer\":\"romance_analyzer\",\"tags\":[\"brand-cascade\",\"language-en\",\"country-ca\"],\"contentTags\":[],\"linkTags\":[],\"lang\":{\"primaryLanguage\":\"en\"}},\"twitter\":{\"retweetCount\":0,\"entities\":{\"symbols\":[],\"urls\":[{\"expanded_url\":\"http://bit.ly/hUmoRz\",\"indices\":[77,99],\"display_url\":\"bit.ly/hUmoRz\",\"urls\":\"http://t.co/Ytn45Pbttk\"}],\"hashtags\":[{\"text\":\"SmurfsVillage\",\"indices\":[100,114]}],\"user_mentions\":[{\"id\":188075479,\"name\":\"Beeline Interactive\",\"indices\":[115,128],\"screen_name\":\"BeelineGames\",\"id_str\":\"188075479\"}]}},\"gnip\":{\"matching_rules\":[{\"tag\":\"cascade_CA_CA_en\"}],\"klout_score\":10,\"urls\":[{\"expanded_url\":\"https://itunes.apple.com/us/app/smurfs-village/id399648212?mt=8\",\"urls\":\"http://t.co/Ytn45Pbttk\"}],\"klout_profile\":{\"topics\":[],\"klout_user_id\":\"257268143479895040\",\"link\":\"http://klout.com/user/id/257268143479895040\"},\"language\":{\"value\":\"fr\"}}},\"id\":\"tag:search.twitter.com,2005:372802927385403392\",\"actor\":{\"id\":\"id:twitter.com:583891967\",\"image\":{\"urls\":\"https://si0.twimg.com/sticky/default_profile_images/default_profile_1_normal.png\"},\"displayName\":\"Sabine Chappuis\",\"objectType\":\"person\",\"attachments\":[],\"upstreamDuplicates\":[],\"downstreamDuplicates\":[],\"twitterTimeZone\":\"Brussels\",\"friendsCount\":6,\"favoritesCount\":0,\"link\":\"http://www.twitter.com/spoffff\",\"postedTime\":\"2012-05-18T15:14:35.000Z\",\"links\":[{\"rel\":\"me\",\"href\":null}],\"listedCount\":0,\"languages\":[\"fr\"],\"verified\":false,\"utcOffset\":\"7200\",\"followersCount\":0,\"preferredUsername\":\"spoffff\",\"statusesCount\":87},\"verb\":\"post\",\"object\":{\"id\":\"object:search.twitter.com,2005:372802927385403392\",\"summary\":\"Le Grand Schtroumpf confirme que la cascade magique n'est \\\"Plus tr?�s loin.\\\" http://t.co/Ytn45Pbttk #SmurfsVillage @BeelineGames\",\"objectType\":\"note\",\"attachments\":[],\"upstreamDuplicates\":[],\"downstreamDuplicates\":[],\"link\":\"http://twitter.com/spoffff/statuses/372802927385403392\",\"postedTime\":\"2013-08-28T19:28:38.000Z\"},\"published\":1377718118000,\"generator\":{\"id\":\"{link}\",\"displayName\":\"Smurfs' Village on iOS\",\"attachments\":[],\"upstreamDuplicates\":[],\"downstreamDuplicates\":[],\"link\":\"https://itunes.apple.com/us/app/smurfs-village/id399648212?mt=8&uo=4\"},\"provider\":{\"id\":\"{link}\",\"displayName\":\"Twitter\",\"objectType\":\"service\",\"attachments\":[],\"upstreamDuplicates\":[],\"downstreamDuplicates\":[],\"link\":\"http://www.twitter.com\"},\"content\":\"Le Grand Schtroumpf confirme soccer que la cascade magique n'est \\\"Plus tr?�s loin.\\\" http://t.co/Ytn45Pbttk #SmurfsVillage @BeelineGames\",\"links\":[],\"guid\":\"A8fccSz7rpKfDJY078VLyw==_201308\",\"link\":\"http://twitter.com/spoffff/statuses/372802927385403392\",\"postedTime\":\"2013-08-28T19:28:38.000Z\",\"objectType\":\"activity\",\"twitter_filter_level\":\"medium\"}\n";
    private static final String LINK_EXPANDER_JSON = "{\"body\":\"Analise baseball Coady W2O Lead, EMEA Twitter Linkedin\\nRyan Flinn Director, Earned Media Twitter Linkedin\\nAdam Cohen W2O Lead, Boston Twitter Linkedin\\nSarah Savage Managing Director, Healthcare Twitter Linkedin\\nCarolyn Wang Practice Lead, Healthcare Twitter Linkedin\\nKathy Keanini Group Director, Strategy Twitter Linkedin\\nRob Cronin Practice Lead, Healthcare Twitter Linkedin\\nAnalise Coady W2O Lead, EMEA Twitter Linkedin\\nRyan Flinn Director, Earned Media Twitter Linkedin\\nAdam Cohen W2O Lead, Boston Twitter Linkedin\\nSarah Savage Managing Director, Healthcare Twitter Linkedin\\nCarolyn Wang Practice Lead, Healthcare Twitter Linkedin\\nKathy Keanini Group Director, Strategy Twitter Linkedin\\nRob Cronin Practice Lead, Healthcare Twitter Linkedin\\nOur Thinkers\\nFull-on enterprise consulting, supercharged by the best analytics in the business.\\nProducts & Services\\nMedia\\nPaid. Earned. Shared. Owned. You name it, we either mastered it or just invented it.\\nthe w2o group\\n© W2O Group 2014\\n\",\"finalUrl\":\"http://www.wcgworld.com/\",\"locale\":null,\"twitterSite\":null,\"urlParts\":[\"www.wcgworld.com\"],\"twitterCreator\":null,\"finalStatusCode\":\"200\",\"author\":null,\"originalUrl\":\"http://www.wcgworld.com/\",\"title\":\"WCG World\",\"description\":\"Find out what makes The WCG Approach second to none\",\"redirects\":[],\"domain\":\"www.wcgworld.com\",\"wasRedirected\":false,\"facebookApp\":null,\"metaData\":{\"content-type\":\"text/html; charset=UTF-8\",\"viewport\":\"width=device-width, initial-scale=1\",\"title\":\"WCG World\",\"og:title\":\"WCG World\",\"og:description\":\"Find out what makes The WCG Approach second to none\",\"content-encoding\":\"UTF-8\",\"x-ua-compatible\":\"IE=edge\",\"og:site_name\":\"WCG World\",\"dc:title\":\"WCG World\",\"og:image\":\"http://www.wcgworld.com/assets/img/home_banner.png\",\"og:urls\":\"http://www.wcgworld.com/\"},\"normalizedUrl\":\"www.wcgworld.com/\",\"keywords\":[\"keywords\",\"news_keywords\"],\"status\":\"SUCCESS\",\"isTracked\":false,\"medium\":null,\"facebookPage\":null,\"failure\":false,\"lastModifiedDate\":null,\"tookInMillis\":110,\"publishedDate\":null,\"siteStatus\":\"ERROR\",\"imageURL\":\"http://www.wcgworld.com/assets/img/home_banner.png\",\"plainText\":null}\n";

    private static List<LanguageTag> tags;
    private static ObjectMapper mapper;


    @BeforeClass
    public static void setUpTags() {
        tags = Lists.newLinkedList();
        Scanner scanner = new Scanner(TestLucenSimpleTaggingProcessor.class.getResourceAsStream("/TestTags.tsv"));
        while(scanner.hasNextLine()) {
            String[] line = scanner.nextLine().split("\t");
            tags.add(new LanguageTag(line[0], line[1], "en"));
        }
        mapper = new ObjectMapper();
    }

    @Test
    public void testSerializability() {
        LuceneSimpleTaggingProcessor processor = new LuceneSimpleTaggingProcessor("testCommunity", new String[]{"test","path"});
        LuceneSimpleTaggingProcessor clone = SerializationUtils.clone(processor);
    }

    @Test
    public void testActivityJsonString() {
        LuceneSimpleTaggingProcessor processor = new LuceneSimpleTaggingProcessor("test", new String[] {"$.content"}, null,tags);
        processor.prepare(null);
        List<StreamsDatum> datums = processor.process(new StreamsDatum(ACTIVITY_JSON));
        assertNotNull(datums);
        assertEquals(1, datums.size());
        StreamsDatum datum = datums.get(0);
        assertNotNull(datum);
        assertNotNull(datum.getDocument());
        assertTrue(datum.getDocument() instanceof String);
        String json = (String)datum.getDocument();
        List<String> tags = JsonPath.read(json, "$.extensions.w2o.tags");
        assertEquals(2, tags.size());
        assertTrue(tags.contains("baseball"));
        assertTrue(tags.contains("soccer"));
        tags = JsonPath.read(json, "$.extensions.w2o.contentTags");
        assertEquals(2, tags.size());
        assertTrue(tags.contains("baseball"));
        assertTrue(tags.contains("soccer"));
    }

    @Test
    public void testW2OActivityJsonString() {
        LuceneSimpleTaggingProcessor processor = new LuceneSimpleTaggingProcessor("test", new String[] {"$.content"}, null,tags);
        processor.prepare(null);
        List<StreamsDatum> datums = processor.process(new StreamsDatum(W2O_ACTIVITY_JSON));
        assertNotNull(datums);
        assertEquals(1, datums.size());
        StreamsDatum datum = datums.get(0);
        assertNotNull(datum);
        assertNotNull(datum.getDocument());
        assertTrue(datum.getDocument() instanceof String);
        String json = (String)datum.getDocument();
        List<String> tags = JsonPath.read(json, "$.extensions.w2o.tags");
        assertEquals(1, tags.size());
        assertTrue(tags.contains("soccer"));
        tags = JsonPath.read(json, "$.extensions.w2o.contentTags");
        assertEquals(1, tags.size());
        assertTrue(tags.contains("soccer"));
    }

    @Test
    public void testLinkExpanderJsonString() {
        LuceneSimpleTaggingProcessor processor = new LuceneSimpleTaggingProcessor("test", new String[] {"$.body"}, null, tags);
        processor.prepare(null);
        List<StreamsDatum> datums = processor.process(new StreamsDatum(LINK_EXPANDER_JSON));
        assertNotNull(datums);
        assertEquals(1, datums.size());
        StreamsDatum datum = datums.get(0);
        assertNotNull(datum);
        assertNotNull(datum.getDocument());
        assertTrue(datum.getDocument() instanceof String);
        String json = (String)datum.getDocument();
        List<String> tags = JsonPath.read(json, "$.extensions.w2o.tags");
        assertEquals(1, tags.size());
        assertTrue(tags.contains("baseball"));
        tags = JsonPath.read(json, "$.extensions.w2o.contentTags");
        assertEquals(1, tags.size());
        assertTrue(tags.contains("baseball"));
    }

    @Test
    public void testActivityObject() throws Exception {
        LuceneSimpleTaggingProcessor processor = new LuceneSimpleTaggingProcessor("test", new String[] {"$.content"}, null,tags);
        processor.prepare(null);
        List<StreamsDatum> datums = processor.process(new StreamsDatum(mapper.readValue(ACTIVITY_JSON, Activity.class)));
        assertNotNull(datums);
        assertEquals(1, datums.size());
        StreamsDatum datum = datums.get(0);
        assertNotNull(datum);
        assertNotNull(datum.getDocument());
        assertTrue(datum.getDocument() instanceof Activity);
        String json = mapper.writeValueAsString(datum.getDocument());
        List<String> tags = JsonPath.read(json, "$.extensions.w2o.tags");
        assertEquals(2, tags.size());
        assertTrue(tags.contains("baseball"));
        assertTrue(tags.contains("soccer"));
        tags = JsonPath.read(json, "$.extensions.w2o.contentTags");
        assertEquals(2, tags.size());
        assertTrue(tags.contains("baseball"));
        assertTrue(tags.contains("soccer"));
    }

    @Test
    public void testW2OActivityObject() throws Exception{
        LuceneSimpleTaggingProcessor processor = new LuceneSimpleTaggingProcessor("test", new String[] {"$.content"}, null,tags);
        processor.prepare(null);
        List<StreamsDatum> datums = processor.process(new StreamsDatum(mapper.readValue(W2O_ACTIVITY_JSON, W2OActivity.class)));
        assertNotNull(datums);
        assertEquals(1, datums.size());
        StreamsDatum datum = datums.get(0);
        assertNotNull(datum);
        assertNotNull(datum.getDocument());
        assertTrue(datum.getDocument() instanceof W2OActivity);
        String json = (String) mapper.writeValueAsString(datum.getDocument());
        List<String> tags = JsonPath.read(json, "$.extensions.w2o.tags");
        assertEquals(1, tags.size());
        assertTrue(tags.contains("soccer"));
        tags = JsonPath.read(json, "$.extensions.w2o.contentTags");
        assertEquals(1, tags.size());
        assertTrue(tags.contains("soccer"));
    }



}
