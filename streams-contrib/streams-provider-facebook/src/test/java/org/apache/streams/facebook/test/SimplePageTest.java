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
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.Objects;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

public class SimplePageTest {
    private final static Logger LOGGER = LoggerFactory.getLogger(SimplePageTest.class);
    private ObjectMapper mapper = StreamsJacksonMapper.getInstance();
    private ObjectNode event;

    private static final String FACEBOOK_JSON= "{\"metadata\":null,\"id\":\"142803045874943\",\"name\":\"Senator Angus S. King, Jr.\",\"category\":\"Government official\",\"createdTime\":null,\"link\":\"https://www.facebook.com/SenatorAngusSKingJr\",\"likes\":10246,\"location\":{\"street\":\"359 Dirksen Senate Office Building\",\"city\":\"Washington, District of Columbia\",\"state\":\"DC\",\"country\":\"United States\",\"zip\":\"20510\",\"latitude\":null,\"longitude\":null,\"text\":null},\"phone\":\"202-224-5344\",\"checkins\":0,\"picture\":null,\"cover\":{\"id\":null,\"source\":\"https://fbcdn-sphotos-g-a.akamaihd.net/hphotos-ak-xpa1/v/t1.0-9/10288792_321537751334804_8200105519500362465_n.jpg?oh=fbcde9b3e1e011dfa3e699628629bc53&oe=546FB617&__gda__=1416717487_3fa5781d7d9c3d58f2bc798a36ac6fc0\",\"offsetY\":9},\"website\":\"http://www.king.senate.gov\",\"talkingAboutCount\":5034,\"accessToken\":null,\"wereHereCount\":0,\"about\":\"Welcome to the official Facebook page of Senator Angus S. King, Jr. (I-ME).\\nhttp://king.senate.gov\\nhttps://twitter.com/SenAngusKing\\nhttps://www.youtube.com/SenatorAngusKing\",\"username\":\"SenatorAngusSKingJr\",\"published\":true,\"communityPage\":false}";

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
        assertThat(page.getLikes(), is(not(nullValue())));
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
        assertThat(activity.getPublished(), is(not(nullValue())));

        Map<String, Objects> extensions = (Map<String, Objects>)activity.getActor().getAdditionalProperties().get("extensions");
        assertThat(extensions.get("mentions"), is(not(nullValue())));
        assertThat(extensions.get("followers"), is(not(nullValue())));

        extensions = (Map<String, Objects>)activity.getAdditionalProperties().get("extensions");
        assertNotNull(extensions.get("facebook"));
    }

    @Test
    public void TestConverter() throws Exception {
        FacebookTypeConverter converter = new FacebookTypeConverter(String.class, Activity.class);
        converter.prepare(null);
        converter.process(new StreamsDatum(FACEBOOK_JSON));
    }
}