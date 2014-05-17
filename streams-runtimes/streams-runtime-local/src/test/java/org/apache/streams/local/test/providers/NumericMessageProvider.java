package org.apache.streams.local.test.providers;

import org.apache.streams.core.StreamsDatum;
import org.apache.streams.core.StreamsProvider;
import org.apache.streams.core.StreamsResultSet;
import org.joda.time.DateTime;

import java.math.BigInteger;
import java.util.Iterator;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Test StreamsProvider that sends out StreamsDatums numbered from 0 to numMessages.
 */
public class NumericMessageProvider implements StreamsProvider {

    protected int numMessages;

    public NumericMessageProvider() {this.numMessages = 0;}
    public NumericMessageProvider(int numMessages) {
        this.numMessages = numMessages;
    }

    public void startStream() {
        // no op
    }

    public StreamsResultSet readCurrent() {
        return new ResultSet();
    }

    public StreamsResultSet readNew(BigInteger sequence) {
        return new ResultSet();
    }

    public StreamsResultSet readRange(DateTime start, DateTime end) {
        return new ResultSet();
    }

    public void prepare(Object configurationObject) {

    }

    public void cleanUp() {

    }

    private class ResultSet extends StreamsResultSet {

        private ResultSet() {
            super(new ConcurrentLinkedQueue<StreamsDatum>());
            for(int i = 0; i < numMessages; i++)
                this.getQueue().add(new StreamsDatum(i));
        }
    }
}
