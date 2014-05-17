package org.apache.streams.local.test.processors;

import org.apache.streams.core.StreamsDatum;
import org.apache.streams.core.StreamsProcessor;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Pass-through processor that counts the elements and stores them in memory
 */
public class PassThroughStaticCounterProcessor implements StreamsProcessor {

    private final AtomicInteger count = new AtomicInteger(0);
    private final int delay;

    public PassThroughStaticCounterProcessor() {
        this(0);
    }

    public PassThroughStaticCounterProcessor(int delay) {
        this.delay = delay;
    }

    /**
     * How many messages we saw
     * @return
     * The number of messages this instance saw
     */
    public int getMessageCount() {
        return this.count.get();
    }

    public List<StreamsDatum> process(StreamsDatum entry) {
        sleepSafely();
        this.count.incrementAndGet();
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
        // claim an id
    }

    public void cleanUp() {

    }

}
