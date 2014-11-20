package org.apache.streams.dropwizard;

import org.apache.streams.core.StreamBuilder;
import org.apache.streams.core.StreamsPersistWriter;
import org.apache.streams.core.StreamsProcessor;
import org.apache.streams.core.StreamsProvider;
import org.apache.streams.local.builders.LocalStreamBuilder;
import org.joda.time.DateTime;

import java.math.BigInteger;
import java.util.Map;

/**
 * Created by sblackmon on 11/20/14.
 */
public class StreamDropwizardBuilder extends LocalStreamBuilder implements StreamBuilder {

    public StreamDropwizardBuilder() {
        super();
    }

    public StreamDropwizardBuilder(Map<String, Object> streamConfig) {
        super(streamConfig);
    }

    public StreamDropwizardBuilder(int maxQueueCapacity) {
        super(maxQueueCapacity);
    }

    public StreamDropwizardBuilder(int maxQueueCapacity, Map<String, Object> streamConfig) {
        super(maxQueueCapacity, streamConfig);
    }

    @Override
    public StreamBuilder newPerpetualStream(String streamId, StreamsProvider provider) {
        return super.newPerpetualStream(streamId, provider);
    }

}
