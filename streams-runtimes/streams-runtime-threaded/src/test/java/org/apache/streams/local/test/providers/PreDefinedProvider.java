package org.apache.streams.local.test.providers;

import org.apache.streams.core.StreamsDatum;
import org.apache.streams.core.StreamsProvider;
import org.apache.streams.core.StreamsResultSet;
import org.apache.streams.util.ComponentUtils;
import org.joda.time.DateTime;

import java.math.BigInteger;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;

public class PreDefinedProvider implements StreamsProvider {

    private final List<StreamsDatum> datums;

    public PreDefinedProvider(List<StreamsDatum> datums) {
        this.datums = datums;
    }

    @Override
    public void startStream() {

    }

    @Override
    public StreamsResultSet readCurrent() {
        final Queue<StreamsDatum> q = new ArrayBlockingQueue<StreamsDatum>(10);
        final StreamsResultSet streamsResultSet = new StreamsResultSet(q, true);

        new Thread(new Runnable() {
            @Override
            public void run() {
                for(StreamsDatum datum : datums)
                    ComponentUtils.offerUntilSuccess(datum, q);
                // it is done
                streamsResultSet.shutDown();
            }
        }).start();


        return streamsResultSet;
    }

    @Override
    public StreamsResultSet readNew(BigInteger sequence) {
        throw new RuntimeException("Method not available");
    }

    @Override
    public StreamsResultSet readRange(DateTime start, DateTime end) {
        throw new RuntimeException("Method not available");
    }

    @Override
    public void prepare(Object configurationObject) {

    }

    @Override
    public void cleanUp() {

    }
}
