package org.apache.streams.local.test.writer;

import org.apache.streams.core.StreamsDatum;
import org.apache.streams.core.StreamsPersistWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is great for helping to debug. It simply takes
 * the datum and writes it to a logger.
 */
public class LoggerWriter implements StreamsPersistWriter {

    private final static Logger LOGGER = LoggerFactory.getLogger(LoggerWriter.class);

    public void write(StreamsDatum entry) {
        LOGGER.info("Writing Datum: {}", entry);
    }

    public void prepare(Object configurationObject) {
        // No Operation
    }

    public void cleanUp() {
        LOGGER.info("Clean up called writer!");
    }
}
