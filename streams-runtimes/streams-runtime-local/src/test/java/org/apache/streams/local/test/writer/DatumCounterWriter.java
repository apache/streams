package org.apache.streams.core.test.writer;

import org.apache.streams.core.StreamsDatum;
import org.apache.streams.core.StreamsPersistWriter;

/**
 * Created by rebanks on 2/18/14.
 */
public class DatumCounterWriter implements StreamsPersistWriter{

    private int counter = 0;

    @Override
    public void write(StreamsDatum entry) {
        ++this.counter;
    }

    @Override
    public void prepare(Object configurationObject) {

    }

    @Override
    public void cleanUp() {
        System.out.println("clean up called");
    }

    public int getDatumsCounted() {
        return this.counter;
    }
}
