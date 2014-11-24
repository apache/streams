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

package org.apache.streams.rss.test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.Lists;
import org.apache.streams.jackson.StreamsJacksonMapper;
import org.apache.streams.pojo.json.Activity;
import org.apache.streams.pojo.json.Actor;
import org.apache.streams.pojo.json.Author;
import org.apache.streams.pojo.json.Provider;
import org.apache.streams.rss.serializer.SyndEntryActivityConverter;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.junit.Test;

import java.net.URL;
import java.util.List;
import java.util.Scanner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class SyndEntryActivitySerizlizerTest {

    private static ObjectMapper mapper = new StreamsJacksonMapper();

    @Test
    public void testJsonData() throws Exception {
        Scanner scanner = new Scanner(this.getClass().getResourceAsStream("/TestSyndEntryJson.txt"));
        List<Activity> activities = Lists.newLinkedList();
        List<ObjectNode> objects = Lists.newLinkedList();

        SyndEntryActivityConverter serializer = new SyndEntryActivityConverter();

        while(scanner.hasNext()) {
            String line = scanner.nextLine();
            System.out.println(line);
            ObjectNode node = (ObjectNode) mapper.readTree(line);

            objects.add(node);
            activities.add(serializer.deserialize(node));
        }

        assertEquals(11, activities.size());

        for(int x = 0; x < activities.size(); x ++) {
            ObjectNode n = objects.get(x);
            Activity a = activities.get(x);

            testActor(n.get("author").asText(), a.getActor());
            testAuthor(n.get("author").asText(), a.getObject().getAuthor());
            testProvider("id:providers:rss", "RSS", a.getProvider());
            testProviderUrl(a.getProvider());
            testVerb("post", a.getVerb());
            testPublished(n.get("publishedDate").asText(), a.getPublished());
            testUrl(n.get("uri").asText(), n.get("link").asText(), a);
        }
    }

    public void testVerb(String expected, String verb) {
        assertEquals(expected, verb);
    }

    public void testPublished(String expected, DateTime published) {
        assertEquals(new DateTime(expected, DateTimeZone.UTC), published);
    }

    public void testActor(String expected, Actor actor) {
        assertEquals("id:rss:null" + ":" + expected, actor.getId());
        assertEquals(expected, actor.getDisplayName());
    }

    public void testAuthor(String expected, Author author) {
        assertEquals(expected, author.getDisplayName());
        assertEquals(expected, author.getId());
    }

    public void testProvider(String expectedId, String expectedDisplay, Provider provider) {
        assertEquals(expectedId, provider.getId());
        assertEquals(expectedDisplay, provider.getDisplayName());
    }

    public void testProviderUrl(Provider provider) {
        URL url = null;

        try {
            url = new URL(provider.getUrl());
            url.toURI();
        } catch(Exception e) {
            System.out.println("Threw an exception while trying to validate URL: " + provider.getUrl());
        }

        assertNotNull(url);
    }

    public void testUrl(String expectedURI, String expectedLink, Activity activity) {
        assertTrue((expectedURI == activity.getUrl() || expectedLink == activity.getUrl()));
        assertTrue((expectedURI == activity.getObject().getUrl() || expectedLink == activity.getObject().getUrl()));
    }
}