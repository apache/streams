package org.apache.streams.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by sblackmon on 1/6/14.
 */
public class StreamHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(StreamHandler.class);

    private volatile StreamState state;

    public void setState(StreamState state) {
        this.state = state;
    }

    public StreamState getState() {
        return this.state;
    }
}
