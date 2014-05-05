package org.apache.streams.urls;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.apache.commons.lang3.SerializationUtils;
import org.apache.streams.core.StreamsDatum;
import org.apache.streams.jackson.StreamsJacksonModule;
import org.apache.streams.pojo.json.Activity;
import org.junit.Ignore;
import org.junit.Test;
import static org.junit.Assert.*;

import java.util.Collections;
import java.util.List;

/**
 * Created by rebanks on 2/27/14.
 */
@Ignore
public class TestLinkResolverProcessor {

    @Test
    public void testLinkResolverSerializability() {
        LinkResolver resolver = new LinkResolver("http://bit.ly/1cX5Rh4");
        LinkResolver clone = SerializationUtils.clone(resolver);
    }

    @Test
    public void testLinkResolverProcessorSerializability() {
        LinkResolverProcessor processor = new LinkResolverProcessor();
        LinkResolverProcessor clone = SerializationUtils.clone(processor);
    }

    @Test
    public void testActivityLinkResolverProcessorBitly() throws Exception {
        testActivityResolverHelper(
                prepareOriginalUrlLinks(Lists.newArrayList("http://bit.ly/1cX5Rh4")),
                prepareFinalUrlLinks(Lists.newArrayList("http://www.wcgworld.com/")));
    }

    @Test
    public void testActivityLinkResolverProcessorGoogle() throws Exception {
        testActivityResolverHelper(
                prepareOriginalUrlLinks(Lists.newArrayList("http://goo.gl/wSrHDA")),
                prepareFinalUrlLinks(Lists.newArrayList("http://www.wcgworld.com/")));
    }

    @Test
    public void testActivityLinkResolverProcessorOwly() throws Exception {
        testActivityResolverHelper(
                prepareOriginalUrlLinks(Lists.newArrayList("http://ow.ly/u4Kte")),
                prepareFinalUrlLinks(Lists.newArrayList("http://www.wcgworld.com/")));
    }

    @Test
    public void testActivityLinkResolverProcessorGoDaddy() throws Exception {
        testActivityResolverHelper(
                prepareOriginalUrlLinks(Lists.newArrayList("http://x.co/3yapt")),
                prepareFinalUrlLinks(Lists.newArrayList("http://www.wcgworld.com/")));
    }

    @Test
    public void testActivityLinkResolverProcessorMulti() throws Exception {
        testActivityResolverHelper(
                prepareOriginalUrlLinks(Lists.newArrayList("http://x.co/3yapt", "http://ow.ly/u4Kte", "http://goo.gl/wSrHDA")),
                prepareFinalUrlLinks(Lists.newArrayList("http://www.wcgworld.com/", "http://www.wcgworld.com/", "http://www.wcgworld.com/")));
    }

    @Test
    public void testActivityLinkResolverProcessorUnwindable() throws Exception {
        testActivityResolverHelper(
                prepareOriginalUrlLinks(Lists.newArrayList("http://bit.ly/1cX5Rh4", "http://nope@#$%")),
                prepareFinalUrlLinks(Lists.newArrayList("http://www.wcgworld.com/")));
    }

    @Test
    public void testStringActivityLinkResolverProcessorBitly() throws Exception {
        testStringActivityResolverHelper(Lists.newArrayList("http://bit.ly/1cX5Rh4"), Lists.newArrayList("http://www.wcgworld.com/"));
    }

    @Test
    public void testStringActivityLinkResolverProcessorGoogle() throws Exception {
        testStringActivityResolverHelper(Lists.newArrayList("http://goo.gl/wSrHDA"), Lists.newArrayList("http://www.wcgworld.com/"));
    }

    @Test
    public void testStringActivityLinkResolverProcessorOwly() throws Exception {
        testStringActivityResolverHelper(Lists.newArrayList("http://ow.ly/u4Kte"), Lists.newArrayList("http://www.wcgworld.com/"));
    }

    @Test
    public void testStringActivityLinkResolverProcessorGoDaddy() throws Exception {
        testStringActivityResolverHelper(Lists.newArrayList("http://x.co/3yapt"), Lists.newArrayList("http://www.wcgworld.com/"));
    }

    @Test
    public void testStringActivityLinkResolverProcessorMulti() throws Exception {
        testStringActivityResolverHelper(Lists.newArrayList("http://x.co/3yapt", "http://ow.ly/u4Kte", "http://goo.gl/wSrHDA"), Lists.newArrayList("http://www.wcgworld.com/", "http://www.wcgworld.com/", "http://www.wcgworld.com/"));
    }

    @Test
    public void testStringActivityLinkResolverProcessorUnwindable() throws Exception {
        testStringActivityResolverHelper(Lists.newArrayList("http://bit.ly/1cX5Rh4", "http://nope@#$%"), Lists.newArrayList("http://www.wcgworld.com/"));
    }

    public void testActivityResolverHelper(List<LinkDetails> input, List<LinkDetails> expected) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        mapper.disable(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        mapper.registerModule(new StreamsJacksonModule());
        Activity activity = new Activity();
        List<Object> objList = Lists.newArrayList();
        Collections.addAll(objList, input);
        activity.setLinks(objList);
        StreamsDatum datum = new StreamsDatum(activity);
        LinkResolverProcessor processor = new LinkResolverProcessor();
        processor.prepare(null);
        List<StreamsDatum> result = processor.process(datum);
        assertNotNull(result);
        assertEquals(1, result.size());
        StreamsDatum resultDatum = result.get(0);
        assertNotNull(resultDatum);
        assertTrue(resultDatum.getDocument() instanceof Activity);
        Activity resultActivity = (Activity) resultDatum.getDocument();
        assertNotNull(resultActivity.getLinks());
        List<LinkDetails> resultLinks = Lists.newArrayList();
        for( Object resultLink : resultActivity.getLinks() ) {
            LinkDetails in = (LinkDetails)resultLink;
            resultLinks.add(in);
        }
        assertEquals(expected.size(), resultLinks.size());
        for( LinkDetails link : resultLinks )
            assertNotNull(link.getFinalURL());
    }

    public void testStringActivityResolverHelper(List<String> input, List<String> expected) throws Exception{
        ObjectMapper mapper = new ObjectMapper();
        mapper.disable(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        mapper.registerModule(new StreamsJacksonModule());
        Activity activity = new Activity();
        List<Object> linkObjs = Lists.newArrayList();
        for( String linkStr : input ) {
            linkObjs.add(linkStr);
        }
        activity.setLinks(linkObjs);
        String str = mapper.writeValueAsString(activity);
        StreamsDatum datum = new StreamsDatum(str);
        LinkResolverProcessor processor = new LinkResolverProcessor();
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
        List<Object> resultLinks = resultActivity.getLinks();
        assertEquals(expected.size(), resultLinks.size());
        assertEquals(Sets.newHashSet(expected), Sets.newHashSet(resultLinks));
    }

    public List<LinkDetails> prepareOriginalUrlLinks(List<String> originalUrls) {
        List<LinkDetails> links = Lists.newArrayList();
        for( String originalUrl : originalUrls ) {
            LinkDetails in = (new LinkDetails());
            in.setOriginalURL(originalUrl);
            links.add(in);
        }
        return links;
    }

    public List<LinkDetails> prepareFinalUrlLinks(List<String> finalUrls) {
        List<LinkDetails> links = Lists.newArrayList();
        for( String finalUrl : finalUrls ) {
            LinkDetails in = (new LinkDetails());
            in.setFinalURL(finalUrl);
            links.add(in);
        }
        return links;
    }

}
