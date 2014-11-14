package org.apache.streams.rss.serializer;

import com.sun.syndication.feed.synd.SyndEntry;
import org.apache.streams.data.ActivityConverterResolver;
import org.apache.streams.exceptions.ActivitySerializerException;

/**
 * Created by sblackmon on 11/13/14.
 */
public class RssConverterResolver implements ActivityConverterResolver {
    
    @Override
    public Class bestSerializer(Class documentClass) throws ActivitySerializerException {
        if( documentClass == SyndEntry.class )
            return SyndEntryActivityConverter.class;
        return null;
    }
}
