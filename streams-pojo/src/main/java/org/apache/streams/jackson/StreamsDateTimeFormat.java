package org.apache.streams.jackson;

/**
 * Supplies a custom date-time format to StreamsJacksonModule
 *
 * Implementations must have a no-argument constructor
 */
public interface StreamsDateTimeFormat {

    public String getFormat();

}
