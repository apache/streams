package org.apache.streams.facebook.serializer;

import org.apache.streams.data.ActivitySerializer;
import org.apache.streams.exceptions.ActivitySerializerException;
import org.apache.streams.facebook.Post;
import org.apache.streams.pojo.json.Activity;
import org.apache.streams.pojo.json.Actor;
import org.apache.streams.pojo.json.Provider;

import java.util.List;

/**
 * Converts {@link org.apache.streams.facebook.Post} to {@link org.apache.streams.pojo.json.Activity}
 */
public class FacebookStreamsPostSerializer implements ActivitySerializer<Post> {

    private static final String FACEBOOK_STREAMS_ID = "id:provider:facebook";
    private static final String ID_PREFIX = "id:facebook:";
    private static final String PROVIDER_DISPLAY = "Facebook";

    @Override
    public String serializationFormat() {
        return null;
    }

    @Override
    public Post serialize(Activity deserialized) throws ActivitySerializerException {
        return null;
    }

    @Override
    public Activity deserialize(Post post) throws ActivitySerializerException {
        Activity activity = new Activity();
        activity.setActor(createActor(post));

        activity.setId(post.getId());
        activity.setContent(post.getMessage());
        return null;
    }

    @Override
    public List<Activity> deserializeAll(List<Post> serializedList) {
        return null;
    }

    public Actor createActor(Post post) {
        Actor actor = new Actor();
        actor.setDisplayName(post.getFrom().getName());
        actor.setId(ID_PREFIX+post.getFrom().getId());
        return actor;
    }

    public Provider createProvider(Post post) {
        Provider provider = new Provider();
        provider.setId(FACEBOOK_STREAMS_ID);
        provider.setDisplayName(PROVIDER_DISPLAY);
        return provider;
    }

}
