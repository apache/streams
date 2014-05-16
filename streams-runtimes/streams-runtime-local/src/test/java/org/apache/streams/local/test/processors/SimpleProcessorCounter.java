package org.apache.streams.local.test.processors;

import org.apache.streams.core.StreamsDatum;
import org.apache.streams.core.StreamsProcessor;

import java.util.*;

/**
 * Unlike the other implementation, this doesn't record anything statically
 */
public class SimpleProcessorCounter implements StreamsProcessor {

    private int count = 0;
    private int delay;

    public SimpleProcessorCounter() {
        this(0);
    }

    public SimpleProcessorCounter(int delay) {
        this.delay = delay;
    }

    /**
     * How many messages we saw
     * @return
     * The number of messages this instance saw
     */
    public int getMessageCount() {
        return this.count;
    }

    public List<StreamsDatum> process(StreamsDatum entry) {
        sleepSafely();
        this.count++;
        List<StreamsDatum> result = new LinkedList<StreamsDatum>();
        result.add(entry);
        return result;
    }

    private void sleepSafely() {
        try {
            Thread.sleep(this.delay);
        }
        catch(InterruptedException ie) {
            // no Operation
        }
    }


    public void prepare(Object configurationObject) {
        // noOperation
    }

    public void cleanUp() {

    }

}
