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

package org.apache.streams.facebook.test;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.streams.core.StreamsDatum;
import org.apache.streams.facebook.Page;
import org.apache.streams.facebook.api.FacebookPageActivitySerializer;
import org.apache.streams.facebook.processor.FacebookTypeConverter;
import org.apache.streams.jackson.StreamsJacksonMapper;
import org.apache.streams.pojo.json.Activity;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;

/**
 * Tests conversion of Facebook Page inputs to Actor
 */
@Ignore("ignore until test resources are available.")
public class SimplePageTest {
    private final static Logger LOGGER = LoggerFactory.getLogger(SimplePageTest.class);
    private ObjectMapper mapper = StreamsJacksonMapper.getInstance();
    private ObjectNode event;

    private static final String FACEBOOK_JSON= "{\"about\":\"The Facebook Page celebrates how our friends inspire us, support us, and help us discover the world when we connect.\",\"username\":\"facebook\",\"birthday\":\"02/04/2004\",\"category\":\"Product/Service\",\"can_checkin\":true,\"can_post\":false,\"checkins\":12,\"cover\":{\"id\":\"10154345508521729\",\"source\":\"https://scontent.xx.fbcdn.net/v/t1.0-9/s720x720/12573693_10154345508521729_8347370496501004621_n.png?oh=b75f9ec17e7e8d6c84658f5a1eb1f724&oe=58BFB505\",\"cover_id\":\"10154345508521729\",\"offset_x\":0,\"offset_y\":0},\"display_subtext\":\"12 people checked in here\",\"displayed_message_response_time\":\"AUTOMATIC\",\"engagement\":{\"count\":174813083,\"social_sentence\":\"You and 174M others like this.\"},\"fan_count\":174813083,\"founded\":\"February 4, 2004\",\"general_info\":\"Your ideas and suggestions help us to constantly improve Facebook’s features. Let us know how we can improve your experience.  \\n \\nwww.facebook.com/help/feedback\\n\",\"global_brand_root_id\":\"1499730620315598\",\"id\":\"20531316728\",\"is_community_page\":false,\"is_always_open\":false,\"is_permanently_closed\":false,\"is_published\":true,\"is_unclaimed\":false,\"is_webhooks_subscribed\":false,\"leadgen_tos_accepted\":false,\"link\":\"https://www.facebook.com/facebook/\",\"mission\":\"Founded in 2004, Facebook’s mission is to give people the power to share and make the world more open and connected. People use Facebook to stay connected with friends and family, to discover what’s going on in the world, and to share and express what matters to them.\",\"parking\":{\"lot\":0,\"street\":0,\"valet\":0},\"name\":\"Facebook\",\"name_with_location_descriptor\":\"Facebook\",\"overall_star_rating\":0,\"rating_count\":0,\"talking_about_count\":367210,\"voip_info\":{\"has_mobile_app\":false,\"has_permission\":false,\"is_callable\":false,\"is_callable_webrtc\":false,\"is_pushable\":false,\"reason_code\":1356044,\"reason_description\":\"This person isn't available right now.\"},\"verification_status\":\"blue_verified\",\"website\":\"http://www.facebook.com\",\"were_here_count\":0,\"app_links\":{\"android\":[{\"app_name\":\"Facebook\",\"package\":\"com.facebook.katana\",\"url\":\"fb://page/20531316728\"}],\"ios\":[{\"app_name\":\"Facebook\",\"app_store_id\":\"284882215\",\"url\":\"fb://page/?id=20531316728\"}]},\"featured_video\":{\"updated_time\":\"2016-05-17T15:57:33+0000\",\"description\":\"Explore Grand Central Terminal and the stories that unfold there in the first film shot with the new Facebook Surround 360 camera. Watch the film in standard monoscopic 360 here, or find it in the Oculus Video app to watch in full 3D-360 with Gear VR.\",\"id\":\"10154659446236729\"},\"context\":{\"friends_who_like\":{\"summary\":{\"total_count\":0,\"social_sentence\":\"0 of your friends like this.\"},\"data\":[]},\"id\":\"b3Blbl9ncmFwaF9jb250ZAXh0OjIwNTMxMzE2NzI4\"},\"global_brand_page_name\":\"Facebook\",\"has_added_app\":false,\"is_verified\":true}\n";

    private FacebookPageActivitySerializer facebookPageActivitySerializer = new FacebookPageActivitySerializer();

    @Before
    public void setUp() throws Exception {
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, Boolean.TRUE);
        mapper.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, Boolean.TRUE);
        mapper.configure(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT, Boolean.TRUE);

        InputStream is = SimplePageTest.class.getResourceAsStream("/testpage.json");
        InputStreamReader isr = new InputStreamReader(is);
        BufferedReader br = new BufferedReader(isr);

        event = null;
        event = (ObjectNode) mapper.readTree(FACEBOOK_JSON);
    }

    @Test
    public void TestSerialization() {
        assertThat(event, is(not(nullValue())));

        Page page = mapper.convertValue(event, Page.class);

        assertThat(page, is(not(nullValue())));
        assertThat(page.getAbout(), is(not(nullValue())));
        assertThat(page.getFanCount(), is(not(nullValue())));
        assertThat(page.getTalkingAboutCount(), is(not(nullValue())));
    }

    @Test
    public void TestDeserialization() throws Exception {
        Page page = mapper.convertValue(event, Page.class);

        Activity activity = null;
        activity = facebookPageActivitySerializer.deserialize(page);

        assertThat(activity, is(not(nullValue())));

        assertThat(activity.getActor(), is(not(nullValue())));
        assertThat(activity.getActor().getId(), is(not(nullValue())));
        assertThat(activity.getVerb(), is(not(nullValue())));
        assertThat(activity.getProvider(), is(not(nullValue())));
    }

    @Test
    public void TestConverter() throws Exception {
        FacebookTypeConverter converter = new FacebookTypeConverter(String.class, Activity.class);
        converter.prepare(null);
        converter.process(new StreamsDatum(FACEBOOK_JSON));
    }
}