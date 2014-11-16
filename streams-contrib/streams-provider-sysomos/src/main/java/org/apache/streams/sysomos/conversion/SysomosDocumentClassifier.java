package org.apache.streams.sysomos.conversion;

import com.sysomos.xml.BeatApi;
import org.apache.streams.data.DocumentClassifier;

/**
 * Ensures sysomos documents can be converted to Activity
 */
public class SysomosDocumentClassifier implements DocumentClassifier {
    @Override
    public Class detectClass(Object document) {
        if( document instanceof BeatApi.BeatResponse.Beat )
            return BeatApi.BeatResponse.Beat.class;
        else return null;
    }
}
