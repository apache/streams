package org.apache.streams.local.test.providers;

import org.apache.streams.core.StreamsDatum;
import org.apache.streams.core.StreamsResultSet;
import org.joda.time.DateTime;
import org.omg.SendingContext.RunTime;

import java.math.BigInteger;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Created by rdouglas on 5/16/14.
 */
public class NumericMessageThrowExceptionProvider extends NumericMessageProvider {

    private final int numErrorsToThrow;
    private int numErrorsThrown;

    public NumericMessageThrowExceptionProvider(int numMessages, int numErrorsToThrow) {
        super(numMessages);

        //Throw at least one error
        this.numErrorsToThrow = numErrorsToThrow <= 0 ? 1 : numErrorsToThrow;
        this.numErrorsThrown = 0;
    }

    @Override
    public StreamsResultSet readCurrent() {
        return new ResultSet(this.numMessages);
    }

    @Override
    public StreamsResultSet readNew(BigInteger sequence) {
        return new ResultSet(this.numMessages);
    }

    @Override
    public StreamsResultSet readRange(DateTime start, DateTime end) {
        return new ResultSet(this.numMessages);
    }

    private class ResultSet extends StreamsResultSet {

        private ResultSet(int numMessages) {
            super(new ConcurrentLinkedQueue<StreamsDatum>());
            for(int i = 0; i < numMessages; i++)
                if(numErrorsThrown++ < numErrorsToThrow) {
                    throw new RuntimeException();
                } else {
                    this.getQueue().add(new StreamsDatum(i));
                }
        }
    }
}
