package org.apache.streams.test.component;

import org.apache.streams.core.StreamsDatum;

/**
 * Created by rebanks on 2/28/14.
 */
public class StringToDocumentConverter implements StreamsDatumConverter {

    @Override
    public StreamsDatum convert(String s) {
        return new StreamsDatum(s);
    }

}
