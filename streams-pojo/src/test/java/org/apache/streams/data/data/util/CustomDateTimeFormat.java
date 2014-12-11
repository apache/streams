package org.apache.streams.data.data.util;

import org.apache.streams.jackson.StreamsDateTimeFormat;

/**
 * Supporting class for {@link org.apache.streams.data.data.util.CustomDateTimeFormatTest}
 */
public class CustomDateTimeFormat implements StreamsDateTimeFormat {

    @Override
    public String getFormat() {
        return "EEE MMM dd HH:mm:ss Z yyyy";
    }
}
