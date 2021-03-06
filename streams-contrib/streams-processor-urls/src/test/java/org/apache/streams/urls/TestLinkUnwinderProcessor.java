/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.streams.urls;

import org.apache.streams.core.StreamsDatum;
import org.apache.streams.jackson.StreamsJacksonMapper;
import org.apache.streams.pojo.json.Activity;

import org.apache.commons.lang3.SerializationUtils;
import org.junit.Ignore;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class TestLinkUnwinderProcessor {

    private static String activityString;

    @Test
    public void testLinkUnwinderBadDomain() {
        LinkResolver resolver = new LinkResolver("http://nope@#$%");
        resolver.run();
        LinkDetails details = resolver.getLinkDetails();
        assertEquals("Should be 400", LinkDetails.LinkStatus.MALFORMED_URL, details.getLinkStatus());
    }

    @Ignore
    @Test
    public void testLinkResolverSerializability() {
        LinkResolver resolver = new LinkResolver("http://bit.ly/1cX5Rh4");
        LinkResolver clone = SerializationUtils.clone(resolver);
    }

    @Ignore
    @Test
    public void test404Link() {
        LinkResolver resolver = new LinkResolver("http://www.kneesupmotherbrown.me/2013/05/26/well-its-fair-to-say-may-has-been-a-crappy-month");
        resolver.run();
        LinkDetails details = resolver.getLinkDetails();
        assertEquals("Should be 404", LinkDetails.LinkStatus.NOT_FOUND, details.getLinkStatus());
    }

    @Test
    public void testLinkResolverProcessorSerializability() {
        LinkResolverProcessor processor = new LinkResolverProcessor();
        LinkResolverProcessor clone = SerializationUtils.clone(processor);
    }

    @Ignore
    @Test
    public void testActivityLinkUnwinderProcessorBitly() throws Exception{
        testActivityUnwinderHelper(Collections.singletonList("http://bit.ly/1cX5Rh4"), Collections.singletonList("http://www.wcgworld.com/"));
        testStringActivityUnwinderHelper(Collections.singletonList("http://bit.ly/1cX5Rh4"), Collections.singletonList("http://www.wcgworld.com/"));
    }

    @Ignore
    @Test
    public void testActivityLinkUnwinderProcessorTdotCo() throws Exception{
        testActivityUnwinderHelper(Collections.singletonList("http://t.co/lLFgFynv2G"), Collections.singletonList("http://www.holmesreport.com/latest"));
        testStringActivityUnwinderHelper(Collections.singletonList("http://t.co/lLFgFynv2G"), Collections.singletonList("http://www.holmesreport.com/latest"));
    }

    @Ignore
    @Test
    public void testActivityLinkUnwinderProcessorGoogle() throws Exception{
        testActivityUnwinderHelper(Collections.singletonList("http://goo.gl/wSrHDA"), Collections.singletonList("http://www.wcgworld.com/"));
        testStringActivityUnwinderHelper(Collections.singletonList("http://goo.gl/wSrHDA"), Collections.singletonList("http://www.wcgworld.com/"));
    }

    @Ignore
    @Test
    public void testActivityLinkUnwinderProcessorOwly() throws Exception{
        testActivityUnwinderHelper(Collections.singletonList("http://ow.ly/u4Kte"), Collections.singletonList("http://www.wcgworld.com/"));
        testStringActivityUnwinderHelper(Collections.singletonList("http://ow.ly/u4Kte"), Collections.singletonList("http://www.wcgworld.com/"));
    }

    @Ignore
    @Test
    public void testActivityLinkUnwinderProcessorGoDaddy() throws Exception{
        testActivityUnwinderHelper(Collections.singletonList("http://x.co/3yapt"), Collections.singletonList("http://www.wcgworld.com/"));
        testStringActivityUnwinderHelper(Collections.singletonList("http://x.co/3yapt"), Collections.singletonList("http://www.wcgworld.com/"));
    }

    @Ignore
    @Test
    public void testActivityLinkUnwinderProcessorMulti() throws Exception{
        // changed these tests because the processor now guarantees each result returned only once
        testActivityUnwinderHelper(Arrays.asList("http://x.co/3yapt", "http://ow.ly/u4Kte", "http://goo.gl/wSrHDA"), Collections.singletonList("http://www.wcgworld.com/"));
        testStringActivityUnwinderHelper(Arrays.asList("http://x.co/3yapt", "http://ow.ly/u4Kte", "http://goo.gl/wSrHDA"), Collections.singletonList("http://www.wcgworld.com/"));
    }

    public void testActivityUnwinderHelper(List<String> input, List<String> expected) throws Exception{

        // Purge all of the domain wait times (for testing)
        LinkResolverHelperFunctions.purgeAllDomainWaitTimes();

        // Create a new activity
        Activity activity = new Activity();
        activity.setLinks(input);
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
        List<String> resultLinks = resultActivity.getLinks();
        assertEquals(expected.size(), resultLinks.size());
        assertEquals(expected, resultLinks);
    }

    public void testStringActivityUnwinderHelper(List<String> input, List<String> expected) throws Exception{
        LinkResolverHelperFunctions.purgeAllDomainWaitTimes();
        Activity activity = new Activity();
        activity.setLinks(input);
        String str = StreamsJacksonMapper.getInstance().writeValueAsString(activity);
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
        Activity resultActivity = StreamsJacksonMapper.getInstance().readValue(resultActivityString, Activity.class);
        assertNotNull(resultActivity.getLinks());
        List<String> resultLinks = resultActivity.getLinks();
        assertEquals(expected.size(), resultLinks.size());
        assertEquals(expected, resultLinks);
    }

}
