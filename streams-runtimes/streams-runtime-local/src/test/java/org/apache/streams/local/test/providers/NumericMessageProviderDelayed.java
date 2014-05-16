package org.apache.streams.local.test.providers;

import org.apache.streams.core.StreamsDatum;
import org.apache.streams.core.StreamsProvider;
import org.apache.streams.core.StreamsResultSet;
import org.apache.streams.util.ComponentUtils;
import org.joda.time.DateTime;

import java.math.BigInteger;
import java.util.Queue;
import java.util.concurrent.*;

public class NumericMessageProviderDelayed implements StreamsProvider {

    private final int numMessages;
    private final int delay;
    protected final Queue<StreamsDatum> queue = new ArrayBlockingQueue<StreamsDatum>(500);

    public NumericMessageProviderDelayed(int numMessages) {
        this(numMessages, 0);
    }

    public NumericMessageProviderDelayed(int numMessages, int delay) {
        this.numMessages = numMessages;
        this.delay = delay;
    }

    public void startStream() {
        // no op
    }

    public StreamsResultSet readCurrent() {
        StreamsResultSet streamsResultSet = new StreamsResultSet(this.queue, true);
        new Thread(new LeakNumbers(streamsResultSet)).start();
        return streamsResultSet;
    }

    public StreamsResultSet readNew(BigInteger sequence) {
        StreamsResultSet streamsResultSet = new StreamsResultSet(this.queue, true);
        new Thread(new LeakNumbers(streamsResultSet)).start();
        return streamsResultSet;
    }

    public StreamsResultSet readRange(DateTime start, DateTime end) {
        StreamsResultSet streamsResultSet = new StreamsResultSet(this.queue, true);
        new Thread(new LeakNumbers(streamsResultSet)).start();
        return streamsResultSet;
    }

    public void prepare(Object configurationObject) {

    }

    public void cleanUp() {

    }

    class LeakNumbers implements Runnable {

        private final StreamsResultSet streamsResultSet;

        LeakNumbers(StreamsResultSet streamsResultSet) {
            this.streamsResultSet = streamsResultSet;
        }

        public void run() {
            collectIdsAndPlaceOnQueue();
        }

        private void collectIdsAndPlaceOnQueue() {
            final int threadCount = 5;
            final int maxWaitingCount = threadCount * 3;

            ThreadPoolExecutor executorService =  new ThreadPoolExecutor(threadCount,
                    maxWaitingCount,
                    0L,
                    TimeUnit.MILLISECONDS,
                    new LinkedBlockingQueue<Runnable>(maxWaitingCount),
                    new WaitRejectedExecutionHandler());

            for(int i = 0; i < numMessages; i++) {
                final int toOffer = i;
                executorService.execute(new Runnable() {
                    public void run() {
                        safeSleep();
                        ComponentUtils.offerUntilSuccess(new StreamsDatum(toOffer), queue);
                    }
                });
            }

            try
            {
                // Shut down our thread pool
                executorService.shutdown();

                // wait for the thread pool to finish executing
                executorService.awaitTermination(10, TimeUnit.MINUTES);
            }
            catch(InterruptedException e) {
                // no operation
            }
            finally {
                // Shutdown the result set
                streamsResultSet.shutDown();
            }
        }
    }

    class WaitRejectedExecutionHandler implements RejectedExecutionHandler {
        public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
            // Wait until the pool is free for another item
            while(executor.getMaximumPoolSize() == executor.getQueue().size()) {
                safeSleep();
            }
            executor.submit(r);
        }
    }

    public static void safeSleep()
    {
        Thread.yield();
        try {
            // wait one tenth of a millisecond
            Thread.sleep(0, (1000000 / 10));
            Thread.yield();
        }
        catch(Exception e) {
            // no operation
        }
        Thread.yield();
    }

}
