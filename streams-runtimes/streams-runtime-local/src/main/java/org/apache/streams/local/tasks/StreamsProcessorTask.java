package org.apache.streams.local.tasks;

import org.apache.streams.core.StreamsDatum;
import org.apache.streams.core.StreamsProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 *
 */
public class StreamsProcessorTask extends BaseStreamsTask {

    private final static Logger LOGGER = LoggerFactory.getLogger(StreamsProviderTask.class);

    private StreamsProcessor processor;
    private long sleepTime;
    private AtomicBoolean keepRunning;
    private Map<String, Object> streamConfig;
    private Queue<StreamsDatum> inQueue;
    private AtomicBoolean isRunning;

    /**
     * Default constructor, uses default sleep time of 500ms when inbound queue is empty
     * @param processor process to run in task
     */
    public StreamsProcessorTask(StreamsProcessor processor) {
        this(processor, DEFAULT_SLEEP_TIME_MS);
    }

    /**
     *
     * @param processor processor to run in task
     * @param sleepTime time to sleep when incoming queue is empty
     */
    public StreamsProcessorTask(StreamsProcessor processor, long sleepTime) {
        this.processor = processor;
        this.sleepTime = sleepTime;
        this.keepRunning = new AtomicBoolean(true);
        this.isRunning = new AtomicBoolean(true);
    }

    @Override
    public void stopTask() {
        this.keepRunning.set(false);
    }

    @Override
    public void setStreamConfig(Map<String, Object> config) {
        this.streamConfig = config;
    }

    @Override
    public void addInputQueue(Queue<StreamsDatum> inputQueue) {
        this.inQueue = inputQueue;
    }

    @Override
    public boolean isRunning() {
        return this.isRunning.get();
    }

    @Override
    public void run() {
        try {
            this.processor.prepare(this.streamConfig);

            while(this.keepRunning.get()) {
                if(!this.keepRunning.get()) {
                    // this is a hard kill we are going to break.
                    LOGGER.info("Processor Terminated while executing: {}", this.processor);
                    break;
                }

                // we don't have anything to do, let's yield
                // and take a quick rest and wait for people to
                // catch up
                if(this.inQueue.size() == 0)
                    safeQuickRest();

                // try to get the output from the processor
                StreamsDatum datum = null;
                while((datum = this.inQueue.poll()) != null) {
                    try {
                        List<StreamsDatum> output = this.processor.process(datum);
                        if (output != null)
                            for (StreamsDatum outDatum : output)
                                super.addToOutgoingQueue(outDatum);
                    } catch (Exception e) {
                        LOGGER.warn("There was an error processing datum: {}", datum);
                    } catch (Throwable e) {
                        LOGGER.warn("There was an error processing datum: {}", datum);
                    }
                }
            }
        } finally {
            // clean everything up
            this.processor.cleanUp();
            this.isRunning.set(false);
        }
    }

    @Override
    public List<Queue<StreamsDatum>> getInputQueues() {
        List<Queue<StreamsDatum>> queues = new LinkedList<Queue<StreamsDatum>>();
        queues.add(this.inQueue);
        return queues;
    }
}
