package org.apache.streams.data.data.util;

import org.apache.streams.jackson.StreamsDateTimeFormat;

/**
 * Created by sblackmon on 12/1/14.
 */
public class CustomDateTimeFormat implements StreamsDateTimeFormat {

    @Override
    public String getFormat() {
        return "EEE MMM dd HH:mm:ss Z yyyy";
    }
}
