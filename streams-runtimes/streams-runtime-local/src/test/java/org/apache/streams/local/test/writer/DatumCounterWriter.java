package org.apache.streams.local.test.writer;

import org.apache.streams.core.StreamsDatum;
import org.apache.streams.core.StreamsPersistWriter;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * A simple counter to count how many times the 'write' was
 * called.
 */
public class DatumCounterWriter implements StreamsPersistWriter{

    protected final AtomicInteger counter = new AtomicInteger(0);
    private final int delayInMilliseconds;

    public DatumCounterWriter() {
        this(0);
    }

    public DatumCounterWriter(int delayInMilliseconds) {
        this.delayInMilliseconds = delayInMilliseconds;
    }

    protected void safeSleep() {
        if(this.delayInMilliseconds > 0) {
            try {
                Thread.sleep(this.delayInMilliseconds);
            } catch (InterruptedException ie) {
                // no Operation
            }
        }
    }

    public void write(StreamsDatum entry) {
        safeSleep();
        this.counter.incrementAndGet();
    }

    public void prepare(Object configurationObject) {

    }

    public void cleanUp() {

    }

    public int getDatumsCounted() {
        return this.counter.get();
    }
}
