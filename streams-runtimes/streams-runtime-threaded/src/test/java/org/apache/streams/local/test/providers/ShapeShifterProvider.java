package org.apache.streams.local.test.providers;

import org.apache.streams.core.StreamsDatum;
import org.apache.streams.core.StreamsProvider;
import org.apache.streams.core.StreamsResultSet;
import org.joda.time.DateTime;

import java.math.BigInteger;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;

public class ShapeShifterProvider implements StreamsProvider {

    protected final int startNumber;
    protected final int numMessages;
    protected final int shiftEvery;

    private final AtomicBoolean running = new AtomicBoolean(true);
    private StreamsResultSet resultSet;

    public ShapeShifterProvider() {
        this(0,0,0);
    }

    public ShapeShifterProvider(int numMessages, int shiftEvery) {
        this(0,numMessages,shiftEvery);
    }

    public ShapeShifterProvider(int startNumber, int numMessages, int shiftEvery) {
        this.startNumber = startNumber;
        this.numMessages = numMessages;
        this.shiftEvery = shiftEvery;
    }


    public void startStream() {
        // no op
        resultSet = new ResultSet();
    }

    public StreamsResultSet readCurrent() {
        return this.resultSet;
    }

    public StreamsResultSet readNew(BigInteger sequence) {
        return this.resultSet;
    }

    public StreamsResultSet readRange(DateTime start, DateTime end) {
        return this.resultSet;
    }

    @Override
    public boolean isRunning() {
        return this.running.get();
    }

    public void prepare(Object configurationObject) {

    }

    public void cleanUp() {
    }

    private class ResultSet extends StreamsResultSet {

        private ResultSet() {
            super(new ConcurrentLinkedQueue<StreamsDatum>());
            for(int i = 0; i < numMessages; i++) {
                Object toEmit;
                if(((i + shiftEvery) / shiftEvery) % 2 == 1)
                    toEmit = new NumericMessageObject(startNumber + i);
                else
                    toEmit = new NumericStringMessageObject(startNumber + i);

                this.getQueue().add(new StreamsDatum(toEmit));
            }

            running.set(false);
        }
    }
}
