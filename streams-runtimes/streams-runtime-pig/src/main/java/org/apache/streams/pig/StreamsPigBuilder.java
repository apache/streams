package org.apache.streams.pig;

import org.apache.streams.core.StreamBuilder;
import org.apache.streams.core.StreamsPersistWriter;
import org.apache.streams.core.StreamsProcessor;
import org.apache.streams.core.StreamsProvider;
import org.joda.time.DateTime;

import java.math.BigInteger;

/**
 * Created by sblackmon on 3/25/14.
 */
public class StreamsPigBuilder implements StreamBuilder {
    @Override
    public StreamBuilder addStreamsProcessor(String s, StreamsProcessor streamsProcessor, int i, String... strings) {
        return null;
    }

    @Override
    public StreamBuilder addStreamsPersistWriter(String s, StreamsPersistWriter streamsPersistWriter, int i, String... strings) {
        return null;
    }

    @Override
    public StreamBuilder newPerpetualStream(String s, StreamsProvider streamsProvider) {
        return null;
    }

    @Override
    public StreamBuilder newReadCurrentStream(String s, StreamsProvider streamsProvider) {
        return null;
    }

    @Override
    public StreamBuilder newReadNewStream(String s, StreamsProvider streamsProvider, BigInteger bigInteger) {
        return null;
    }

    @Override
    public StreamBuilder newReadRangeStream(String s, StreamsProvider streamsProvider, DateTime dateTime, DateTime dateTime2) {
        return null;
    }

    @Override
    public void start() {

    }

    @Override
    public void stop() {

    }
}
