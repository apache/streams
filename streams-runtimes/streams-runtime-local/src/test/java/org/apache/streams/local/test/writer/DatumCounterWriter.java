package org.apache.streams.local.test.writer;

import org.apache.streams.core.StreamsDatum;
import org.apache.streams.core.StreamsPersistWriter;

/**
 * A simple counter to count how many times the 'write' was
 * called.
 */
public class DatumCounterWriter implements StreamsPersistWriter{

    protected int counter = 0;
    private int delayInMilliseconds = 0;

    public DatumCounterWriter() {
        this(0);
    }

    public DatumCounterWriter(int delayInMilliseconds) {
        this.delayInMilliseconds = delayInMilliseconds;
    }

    protected void safeSleep() {
        try {
            Thread.sleep(this.delayInMilliseconds);
        }
        catch(InterruptedException ie) {
            // no Operation
        }
    }

    public void write(StreamsDatum entry) {
        safeSleep();
        this.counter++;
    }

    public void prepare(Object configurationObject) {

    }

    public void cleanUp() {

    }

    public int getDatumsCounted() {
        return this.counter;
    }
}
