package org.apache.streams.data.moreover.conversion;

import com.moreover.Moreover;
import com.moreover.api.Article;
import org.apache.streams.data.ActivityConverterResolver;
import org.apache.streams.exceptions.ActivitySerializerException;

/**
 * Ensures moreover documents can be converted to Activity
 */
public class MoreoverConverterResolver implements ActivityConverterResolver {
    @Override
    public Class bestSerializer(Class documentClass) throws ActivitySerializerException {
        if( documentClass == Moreover.class )
            return MoreoverJsonActivityConverter.class;
        else if( documentClass == Article.class )
            return MoreoverJsonActivityConverter.class;
        else return null;
    }
}
