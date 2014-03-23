package org.apache.streams.core.test.processors;

import org.apache.streams.core.StreamsDatum;
import org.apache.streams.core.StreamsProcessor;

import java.util.*;

/**
 * Created by rebanks on 2/18/14.
 */
public class PassthroughDatumCounterProcessor implements StreamsProcessor {

    public static Set<Integer> claimedNumber = new HashSet<Integer>();
    public static final Random rand = new Random();
    public static Set<Integer> sawData = new HashSet<Integer>();

    private int count = 0;
    private int id;

    @Override
    public List<StreamsDatum> process(StreamsDatum entry) {
        ++this.count;
        List<StreamsDatum> result = new LinkedList<StreamsDatum>();
        result.add(entry);
        synchronized (sawData) {
            sawData.add(this.id);
        }
        return result;
    }

    @Override
    public void prepare(Object configurationObject) {
        synchronized (claimedNumber) {
            this.id = rand.nextInt();
            while(!claimedNumber.add(this.id)) {
                this.id = rand.nextInt();
            }
        }
    }

    @Override
    public void cleanUp() {

    }

    public int getMessageCount() {
        return this.count;
    }
}
