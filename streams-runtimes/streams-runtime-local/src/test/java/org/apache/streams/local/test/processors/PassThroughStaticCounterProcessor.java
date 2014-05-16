package org.apache.streams.local.test.processors;

import org.apache.streams.core.StreamsDatum;
import org.apache.streams.core.StreamsProcessor;

import java.util.*;

/**
 * Pass-through processor that counts the elements and stores them in memory
 */
public class PassThroughStaticCounterProcessor implements StreamsProcessor {

    private static final Random rand = new Random();
    public static final Set<String> CLAIMED_NUMBERS = new HashSet<String>();
    public static final Set<String> SEEN_DATA = new HashSet<String>();

    protected int count = 0;
    protected String id;
    private int delay;

    public PassThroughStaticCounterProcessor() {
        this(0);
    }

    public PassThroughStaticCounterProcessor(int delay) {
        this.delay = delay;
    }

    public static void clear() {
        CLAIMED_NUMBERS.clear();
        SEEN_DATA.clear();
    }

    /**
     * The unique identifier of this particular item
     * @return
     * The unique ID number
     */
    public String getId() {
        return this.id;
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
        SEEN_DATA.add(this.id);
        return result;
    }

    protected void sleepSafely() {
        try {
            Thread.sleep(this.delay);
        }
        catch(InterruptedException ie) {
            // no Operation
        }
    }


    public void prepare(Object configurationObject) {
        // claim an id
        this.id = UUID.randomUUID().toString();
        CLAIMED_NUMBERS.add(this.id);
    }

    public void cleanUp() {

    }

}
