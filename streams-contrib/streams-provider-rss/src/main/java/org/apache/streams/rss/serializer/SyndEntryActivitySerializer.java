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

package org.apache.streams.rss.serializer;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.base.Preconditions;
import org.apache.streams.data.ActivitySerializer;
import org.apache.streams.data.util.RFC3339Utils;
import org.apache.streams.jackson.StreamsJacksonMapper;
import org.apache.streams.pojo.json.Activity;
import org.apache.streams.pojo.json.ActivityObject;
import org.apache.streams.pojo.json.Actor;
import org.apache.streams.pojo.json.Author;
import org.apache.streams.pojo.json.Provider;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedList;
import java.util.List;

public class SyndEntryActivitySerializer implements ActivitySerializer<ObjectNode> {

    private static final Logger LOGGER = LoggerFactory.getLogger(SyndEntryActivitySerializer.class);

    private boolean includeRomeExtension;

    public SyndEntryActivitySerializer() {
        this(true);
    }

    public SyndEntryActivitySerializer(boolean includeRomeExtension) {
        this.includeRomeExtension = includeRomeExtension;
    }


    @Override
    public List<Activity> deserializeAll(List<ObjectNode> objectNodes) {
        List<Activity> result = new LinkedList<>();
        for (ObjectNode node : objectNodes) {
            result.add(deserialize(node));
        }
        return result;
    }

    @Override
    public String serializationFormat() {
        return "application/streams-provider-rss";
    }

    @Override
    public ObjectNode serialize(Activity deserialized) {
        throw new UnsupportedOperationException("Cannot currently serialize to Rome");
    }

    @Override
    public Activity deserialize(ObjectNode syndEntry) {
        return deserializeWithRomeExtension(syndEntry, this.includeRomeExtension);
    }

    public Activity deserializeWithRomeExtension(ObjectNode entry, boolean withExtension) {
        Preconditions.checkNotNull(entry);

        Activity activity = new Activity();
        Provider provider = buildProvider(entry);
        Actor actor = buildActor(entry);
        ActivityObject activityObject = buildActivityObject(entry);

        activityObject.setUrl(provider.getUrl());
        activityObject.setAuthor(actor.getAuthor());

        activity.setUrl(provider.getUrl());
        activity.setProvider(provider);
        activity.setActor(actor);
        activity.setVerb("post");
        activity.setId("id:rss:post:" + activity.getUrl());

        JsonNode published = entry.get("publishedDate");
        if (published != null) {
            try {
                activity.setPublished(RFC3339Utils.parseToUTC(published.textValue()));
            } catch (Exception e) {
                LOGGER.warn("Failed to parse date : {}", published.textValue());

                DateTime now = DateTime.now().withZone(DateTimeZone.UTC);
                activity.setPublished(now);
            }
        }

        activity.setUpdated(activityObject.getUpdated());
        activity.setObject(activityObject);

        if (withExtension) {
            activity = addRomeExtension(activity, entry);
        }

        return activity;
    }

    /**
     * Given an RSS entry, extra out the author and actor information and return it
     * in an actor object
     *
     * @param entry
     * @return
     */
    private Actor buildActor(ObjectNode entry) {
        Author author = new Author();
        Actor actor = new Actor();

        if (entry.get("author") != null) {
            author.setId(entry.get("author").textValue());
            author.setDisplayName(entry.get("author").textValue());

            actor.setAuthor(author);
            String uriToSet = entry.get("rssFeed") != null ? entry.get("rssFeed").asText() : null;

            actor.setId("id:rss:" + uriToSet + ":" + author.getId());
            actor.setDisplayName(author.getDisplayName());
        }

        return actor;
    }

    /**
     * Given an RSS object, build the ActivityObject
     *
     * @param entry
     * @return
     */
    private ActivityObject buildActivityObject(ObjectNode entry) {
        ActivityObject activityObject = new ActivityObject();

        JsonNode summary = entry.get("description");
        if (summary != null)
            activityObject.setSummary(summary.textValue());
        else if((summary = entry.get("title")) != null) {
            activityObject.setSummary(summary.textValue());
        }

        return activityObject;
    }

    /**
     * Given an RSS object, build and return the Provider object
     *
     * @param entry
     * @return
     */
    private Provider buildProvider(ObjectNode entry) {
        Provider provider = new Provider();

        String link = null;
        String uri = null;
        String resourceLocation = null;

        if (entry.get("link") != null)
            link = entry.get("link").textValue();
        if (entry.get("uri") != null)
            uri = entry.get("uri").textValue();

        /*
         * Order of precedence for resourceLocation selection
         *
         * 1. Valid URI
         * 2. Valid Link
         * 3. Non-null URI
         * 4. Non-null Link
         */
        if(isValidResource(uri))
            resourceLocation = uri;
        else if(isValidResource(link))
            resourceLocation = link;
        else if(uri != null || link != null) {
            resourceLocation = (uri != null) ? uri : link;
        }

        provider.setId("id:providers:rss");
        provider.setUrl(resourceLocation);
        provider.setDisplayName("RSS");

        return provider;
    }

    /**
     * Tests whether or not the passed in resource is a valid URI
     * @param resource
     * @return boolean of whether or not the resource is valid
     */
    private boolean isValidResource(String resource) {
        return resource != null && (resource.startsWith("http") || resource.startsWith("www"));
    }

    /**
     * Given an RSS object and an existing activity,
     * add the Rome extension to that activity and return it
     *
     * @param activity
     * @param entry
     * @return
     */
    private Activity addRomeExtension(Activity activity, ObjectNode entry) {
        ObjectMapper mapper = StreamsJacksonMapper.getInstance();
        ObjectNode activityRoot = mapper.convertValue(activity, ObjectNode.class);
        ObjectNode extensions = JsonNodeFactory.instance.objectNode();

        extensions.put("rome", entry);
        activityRoot.put("extensions", extensions);

        activity = mapper.convertValue(activityRoot, Activity.class);

        return activity;
    }
}
