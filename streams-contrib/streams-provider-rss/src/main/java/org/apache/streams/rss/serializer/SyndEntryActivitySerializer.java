package org.apache.streams.rss.serializer;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.sun.syndication.feed.synd.SyndEntry;
import org.apache.streams.data.ActivitySerializer;
import org.apache.streams.pojo.json.*;

import java.util.List;

/**
 * Deserializes the Moreover Article XML and converts it to an instance of {@link Activity}
 */
public class SyndEntryActivitySerializer implements ActivitySerializer {


    @Override
    public String serializationFormat() {
        return "application/xml+vnd.moreover.com.v1";
    }

    @Override
    public String serialize(Activity deserialized) {
        throw new UnsupportedOperationException("Cannot currently serialize to Moreover");
    }

    @Override
    public Activity deserialize(String serialized) {
        return null;
    }

    @Override
    public List<Activity> deserializeAll(String serializedList) {
        return null;
    }

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

    public List<Activity> deserializeAll(List<SyndEntry> serializedList) {
        List<Activity> activityList = Lists.newArrayList();
        for(SyndEntry entry : serializedList) {
            activityList.add(deserialize(entry));
        }
        return activityList;
    }


}
