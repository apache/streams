package org.apache.streams.sysomos.conversion;

import com.sysomos.xml.BeatApi;
import org.apache.streams.data.ActivityConverterResolver;
import org.apache.streams.data.DocumentClassifier;
import org.apache.streams.exceptions.ActivitySerializerException;

/**
 * Created by sblackmon on 11/13/14.
 */
public class SysomosConverterResolver implements ActivityConverterResolver {

    @Override
    public Class bestSerializer(Class documentClass) throws ActivitySerializerException {
        if( documentClass == BeatApi.BeatResponse.Beat.class )
            return SysomosBeatActivityConverter.class;
        return null;
    }
}
