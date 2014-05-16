package org.apache.streams.local.test.writer;

import org.apache.streams.core.StreamsDatum;
import org.apache.streams.core.StreamsPersistWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This writer does exactly what you'd expect, nothing.
 */
public class DoNothingWriter implements StreamsPersistWriter {

    private final static Logger LOGGER = LoggerFactory.getLogger(DoNothingWriter.class);

    public void write(StreamsDatum entry) {
    }

    public void prepare(Object configurationObject) {
    }

    public void cleanUp() {
    }
}
