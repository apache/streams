package org.apache.streams.local.tasks;

import org.apache.streams.core.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Processor task that is multi-threaded
 */
public class StreamsProcessorMultiThreadedTask extends StreamsProcessorSingleThreadedTask  {

    private final static Logger LOGGER = LoggerFactory.getLogger(StreamsProcessorMultiThreadedTask.class);

    private final int numThreads;

    /**
     * Default constructor, uses default sleep time of 500ms when inbound queue is empty
     *
     * @param processor process to run in task
     */
    public StreamsProcessorMultiThreadedTask(StreamsProcessor processor, int numThreads) {
        super(processor);
        this.numThreads = numThreads;
    }


    @Override
    public void run() {

        ThreadPoolExecutor executorService = new ThreadPoolExecutor(this.numThreads,
                this.numThreads,
                0L,
                TimeUnit.MILLISECONDS,
                new ArrayBlockingQueue<Runnable>(this.numThreads, false),
                new WaitUntilAvailableExecutionHandler());

        try {

            this.processor.prepare(this.streamConfig);

            while (this.keepRunning.get() || super.isDatumAvailable()) {

                StreamsDatum datum = null;
                while ((datum = super.pollNextDatum()) != null) {
                    final StreamsDatum workingDatum = datum;
                    executorService.execute(new Runnable() {
                        public void run() {
                            processThisDatum(workingDatum);
                        }
                    });
                }

                // we don't have anything to do, let's yield
                // and take a quick rest and wait for people to
                // catch up
                if (!isDatumAvailable())
                    safeQuickRest();
            }

            LOGGER.info("Shutting down threaded processor");
            executorService.shutdown();
            try {
                executorService.awaitTermination(5, TimeUnit.MINUTES);
                // after 5 minutes, these are the poor souls we left in the executor pool.
                statusCounter.incrementStatus(DatumStatus.FAIL, executorService.getPoolSize());
            } catch (InterruptedException ie) {
                LOGGER.warn("There was an issue waiting for the termination of the threaded processor");
            }

        } finally {
            // clean everything up
            this.processor.cleanUp();
            this.isRunning.set(false);
        }
    }
}