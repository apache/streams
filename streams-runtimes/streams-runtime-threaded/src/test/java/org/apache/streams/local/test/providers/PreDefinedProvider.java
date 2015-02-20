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
import java.util.concurrent.atomic.AtomicBoolean;

public class PreDefinedProvider implements StreamsProvider {

    private final List<StreamsDatum> datums;
    private final AtomicBoolean running = new AtomicBoolean(true);
    private StreamsResultSet streamsResultSet;

    public PreDefinedProvider(List<StreamsDatum> datums) {
        this.datums = datums;
    }

    @Override
    public void startStream() {
        this.streamsResultSet = createStream();
    }

    @Override
    public StreamsResultSet readCurrent() {
        return this.streamsResultSet;
    }

    private StreamsResultSet createStream() {
        final Queue<StreamsDatum> q = new ArrayBlockingQueue<StreamsDatum>(10);
        final StreamsResultSet streamsResultSet = new StreamsResultSet(q);

        new Thread(new Runnable() {
            @Override
            public void run() {
                for(StreamsDatum datum : datums)
                    ComponentUtils.offerUntilSuccess(datum, q);
                // it is done
                running.set(false);
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
    public boolean isRunning() {
        return this.running.get();
    }

    @Override
    public void prepare(Object configurationObject) {

    }

    @Override
    public void cleanUp() {
    }
}
