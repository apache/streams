package org.apache.streams.rss.serializer;

/*
 * #%L
 * streams-provider-rss
 * %%
 * Copyright (C) 2013 - 2014 Apache Streams Project
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.sun.syndication.feed.synd.SyndEntry;
import org.apache.streams.data.ActivitySerializer;
import org.apache.streams.pojo.json.*;

import java.util.List;

/**
 * Deserializes the Rome SyndEntry POJO and converts it to an instance of {@link Activity}
 */
public class SyndEntryActivitySerializer implements ActivitySerializer<SyndEntry> {

    @Override
    public String serializationFormat() {
        return "application/streams-provider-rss";
    }

    @Override
    public SyndEntry serialize(Activity deserialized) {
        throw new UnsupportedOperationException("Cannot currently serialize to Rome");
    }

    @Override
    public Activity deserialize(SyndEntry serialized) {
        Preconditions.checkNotNull(serialized);
        Activity activity = new Activity();
        Provider provider = new Provider();
        if( serialized.getSource() != null )
            if( serialized.getSource().getUri() != null )
                provider.setId("rss:"+serialized.getSource().getUri());
        else
            provider.setId("rss:unknown");
        Actor actor = new Actor();
        Author author = new Author();
        if( serialized.getAuthor() != null ) {
            author.setId(serialized.getAuthor());
            author.setDisplayName(serialized.getAuthor());
            actor.setAuthor(author);
        }
        activity.setActor(actor);
        activity.setVerb("blog");
        activity.setProvider(provider);
        ActivityObject activityObject = new ActivityObject();
        activityObject.setSummary(serialized.getTitle());
        activityObject.setUrl(serialized.getLink());
        activity.setObject(activityObject);
        activity.setId(serialized.getLink());
        return activity;
    }

    @Override
    public List<Activity> deserializeAll(List<SyndEntry> serializedList) {
        List<Activity> activityList = Lists.newArrayList();
        for(SyndEntry entry : serializedList) {
            activityList.add(deserialize(entry));
        }
        return activityList;
    }


}
