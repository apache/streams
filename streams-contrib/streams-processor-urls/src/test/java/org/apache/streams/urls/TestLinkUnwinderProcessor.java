package org.apache.streams.urls;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.apache.streams.core.StreamsDatum;
import org.apache.streams.jackson.StreamsJacksonModule;
import org.apache.streams.pojo.json.Activity;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import static org.junit.Assert.*;

import java.util.List;
import java.util.Scanner;

/**
 * Created by rebanks on 2/27/14.
 */
public class TestLinkUnwinderProcessor {

    private static String activityString;

    @Test
    public void testActivityLinkUnwinderProcessorBitly() throws Exception{
        testActivityUnwinderHelper(Lists.newArrayList("http://bit.ly/1cX5Rh4"), Lists.newArrayList("http://www.wcgworld.com/"));
        testStringActivityUnwinderHelper(Lists.newArrayList("http://bit.ly/1cX5Rh4"), Lists.newArrayList("http://www.wcgworld.com/"));
    }

    @Test
    public void testActivityLinkUnwinderProcessorGoogle() throws Exception{
        testActivityUnwinderHelper(Lists.newArrayList("http://goo.gl/wSrHDA"), Lists.newArrayList("http://www.wcgworld.com/"));
        testStringActivityUnwinderHelper(Lists.newArrayList("http://goo.gl/wSrHDA"), Lists.newArrayList("http://www.wcgworld.com/"));
    }

    @Test
    public void testActivityLinkUnwinderProcessorOwly() throws Exception{
        testActivityUnwinderHelper(Lists.newArrayList("http://ow.ly/u4Kte"), Lists.newArrayList("http://www.wcgworld.com/"));
        testStringActivityUnwinderHelper(Lists.newArrayList("http://ow.ly/u4Kte"), Lists.newArrayList("http://www.wcgworld.com/"));
    }

    @Test
    public void testActivityLinkUnwinderProcessorGoDaddy() throws Exception{
        testActivityUnwinderHelper(Lists.newArrayList("http://x.co/3yapt"), Lists.newArrayList("http://www.wcgworld.com/"));
        testStringActivityUnwinderHelper(Lists.newArrayList("http://x.co/3yapt"), Lists.newArrayList("http://www.wcgworld.com/"));
    }

    @Test
    public void testActivityLinkUnwinderProcessorMulti() throws Exception{
        testActivityUnwinderHelper(Lists.newArrayList("http://x.co/3yapt", "http://ow.ly/u4Kte", "http://goo.gl/wSrHDA"), Lists.newArrayList("http://www.wcgworld.com/", "http://www.wcgworld.com/", "http://www.wcgworld.com/"));
        testStringActivityUnwinderHelper(Lists.newArrayList("http://x.co/3yapt", "http://ow.ly/u4Kte", "http://goo.gl/wSrHDA"), Lists.newArrayList("http://www.wcgworld.com/", "http://www.wcgworld.com/", "http://www.wcgworld.com/"));
    }

    @Test
    public void testActivityLinkUnwinderProcessorUnwindable() throws Exception{
        testActivityUnwinderHelper(Lists.newArrayList("http://bit.ly/1cX5Rh4", "http://nope@#$%"), Lists.newArrayList("http://www.wcgworld.com/"));
        testStringActivityUnwinderHelper(Lists.newArrayList("http://bit.ly/1cX5Rh4", "http://nope@#$%"), Lists.newArrayList("http://www.wcgworld.com/"));
    }

    public void testActivityUnwinderHelper(List<String> input, List<String> expected) throws Exception{
        ObjectMapper mapper = new ObjectMapper();
        mapper.disable(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        mapper.registerModule(new StreamsJacksonModule());
        Activity activity = new Activity();
        activity.setLinks(input);
        StreamsDatum datum = new StreamsDatum(activity);
        LinkUnwinderProcessor processor = new LinkUnwinderProcessor();
        processor.prepare(null);
        List<StreamsDatum> result = processor.process(datum);
        assertNotNull(result);
        assertEquals(1, result.size());
        StreamsDatum resultDatum = result.get(0);
        assertNotNull(resultDatum);
        assertTrue(resultDatum.getDocument() instanceof Activity);
        Activity resultActivity = (Activity) resultDatum.getDocument();
        assertNotNull(resultActivity.getLinks());
        List<String> resultLinks = resultActivity.getLinks();
        assertEquals(expected.size(), resultLinks.size());
        assertEquals(Sets.newHashSet(expected), Sets.newHashSet(resultLinks));
    }

    public void testStringActivityUnwinderHelper(List<String> input, List<String> expected) throws Exception{
        ObjectMapper mapper = new ObjectMapper();
        mapper.disable(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        mapper.registerModule(new StreamsJacksonModule());
        Activity activity = new Activity();
        activity.setLinks(input);
        String str = mapper.writeValueAsString(activity);
        StreamsDatum datum = new StreamsDatum(str);
        LinkUnwinderProcessor processor = new LinkUnwinderProcessor();
        processor.prepare(null);
        List<StreamsDatum> result = processor.process(datum);
        assertNotNull(result);
        assertEquals(1, result.size());
        StreamsDatum resultDatum = result.get(0);
        assertNotNull(resultDatum);
        assertTrue(resultDatum.getDocument() instanceof String);
        String resultActivityString = (String) resultDatum.getDocument();
        Activity resultActivity = mapper.readValue(resultActivityString, Activity.class);
        assertNotNull(resultActivity.getLinks());
        List<String> resultLinks = resultActivity.getLinks();
        assertEquals(expected.size(), resultLinks.size());
        assertEquals(Sets.newHashSet(expected), Sets.newHashSet(resultLinks));
    }

}
