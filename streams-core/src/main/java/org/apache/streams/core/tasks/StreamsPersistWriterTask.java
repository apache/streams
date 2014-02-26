package org.apache.streams.core.tasks;

import org.apache.streams.core.StreamsDatum;
import org.apache.streams.core.StreamsPersistWriter;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 *
 */
public class StreamsPersistWriterTask extends BaseStreamsTask {



    private StreamsPersistWriter writer;
    private long sleepTime;
    private AtomicBoolean keepRunning;
    private Map<String, Object> streamConfig;
    private Queue<StreamsDatum> inQueue;
    private AtomicBoolean isRunning;

    /**
     * Default constructor.  Uses default sleep of 500ms when inbound queue is empty.
     * @param writer writer to execute in task
     */
    public StreamsPersistWriterTask(StreamsPersistWriter writer) {
        this(writer, DEFAULT_SLEEP_TIME_MS);
    }

    /**
     *
     * @param writer writer to execute in task
     * @param sleepTime time to sleep when inbound queue is empty.
     */
    public StreamsPersistWriterTask(StreamsPersistWriter writer, long sleepTime) {
        this.writer = writer;
        this.sleepTime = sleepTime;
        this.keepRunning = new AtomicBoolean(true);
        this.isRunning = new AtomicBoolean(true);
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
            this.writer.prepare(this.streamConfig);
            StreamsDatum datum = this.inQueue.poll();
            while(datum != null || this.keepRunning.get()) {
                if(datum != null) {
                    this.writer.write(datum);
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
            this.writer.cleanUp();
            this.isRunning.set(false);
        }
    }

    @Override
    public void stopTask() {
        this.keepRunning.set(false);
    }


    @Override
    public void addOutputQueue(Queue<StreamsDatum> outputQueue) {
        throw new UnsupportedOperationException(this.getClass().getName()+" does not support method - setOutputQueue()");
    }

    @Override
    public List<Queue<StreamsDatum>> getInputQueues() {
        List<Queue<StreamsDatum>> queues = new LinkedList<Queue<StreamsDatum>>();
        queues.add(this.inQueue);
        return queues;
    }
}
