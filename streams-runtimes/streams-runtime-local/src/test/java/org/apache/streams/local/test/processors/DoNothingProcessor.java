package org.apache.streams.local.test.processors;

import org.apache.streams.core.StreamsDatum;
import org.apache.streams.core.StreamsProcessor;

import java.util.LinkedList;
import java.util.List;

/**
 * A processor that does nothing. This processor will
 * drop the message
 */
public class DoNothingProcessor implements StreamsProcessor {

    public List<StreamsDatum> process(StreamsDatum entry) {
        return null;
    }

    public void prepare(Object configurationObject) {
        // no Operation
    }

    public void cleanUp() {
        // no Operation
    }
}
