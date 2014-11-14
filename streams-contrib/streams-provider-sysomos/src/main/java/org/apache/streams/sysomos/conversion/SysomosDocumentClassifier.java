package org.apache.streams.sysomos.conversion;

import com.sysomos.xml.BeatApi;
import org.apache.streams.data.DocumentClassifier;

/**
 * Created by sblackmon on 11/13/14.
 */
public class SysomosDocumentClassifier implements DocumentClassifier {
    @Override
    public Class detectClass(Object document) {
        if( document instanceof BeatApi.BeatResponse.Beat )
            return BeatApi.BeatResponse.Beat.class;
        else return null;
    }
}
