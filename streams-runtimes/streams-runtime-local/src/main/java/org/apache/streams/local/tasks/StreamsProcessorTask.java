package org.apache.streams.local.tasks;

import org.apache.streams.core.StreamsDatum;
import org.apache.streams.core.StreamsProcessor;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 *
 */
public class StreamsProcessorTask extends BaseStreamsTask {


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
            StreamsDatum datum = this.inQueue.poll();
            while(this.keepRunning.get()) {
                if(datum != null) {
                    List<StreamsDatum> output = this.processor.process(datum);
                    if(output != null) {
                        for(StreamsDatum outDatum : output) {
                            super.addToOutgoingQueue(outDatum);
                        }
                    }
                }
                else {
                    try {
                        Thread.sleep(this.sleepTime);
                    } catch (InterruptedException e) {
                        this.keepRunning.set(false);
                    }
                }
                datum = this.inQueue.poll();
            }

        } finally {
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
