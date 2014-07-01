package org.apache.streams.instagram.serializer.util;

import org.jinstagram.Instagram;
import org.jinstagram.exceptions.InstagramException;

/**
 * Created by rdouglas on 7/1/14.
 */
public class InstagramDeserializer extends Instagram{
    public InstagramDeserializer(String test) {
        super(test);
    }

    @Override
    public <T> T createObjectFromResponse(Class<T> clazz, String response) throws InstagramException {
        return super.createObjectFromResponse(clazz, response);
    }
}
