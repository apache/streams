package org.apache.streams.local.test.writer;

import org.apache.streams.core.StreamsDatum;

/**
 * Created by rdouglas on 5/16/14.
 */
public class DatumCounterExceptionWriter extends DatumCounterWriter{
    private final int numErrorsToThrow;
    private int numErrorsThrown;

    public DatumCounterExceptionWriter(int numErrorsToThrow) {
        super(0);

        this.numErrorsToThrow = numErrorsToThrow <= 0 ? 1 : numErrorsToThrow;
        this.numErrorsThrown = 0;
    }

    public void write(StreamsDatum entry) {
        if(numErrorsThrown++ < numErrorsToThrow) {
            throw new RuntimeException();
        } else {
            super.safeSleep();
            super.counter++;
        }
    }
}
