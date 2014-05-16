package org.apache.streams.local.tasks;

import org.apache.streams.core.*;
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
public class StreamsPersistWriterTask extends BaseStreamsTask implements DatumStatusCountable {

    private final static Logger LOGGER = LoggerFactory.getLogger(StreamsPersistWriterTask.class);

    private StreamsPersistWriter writer;
    private long sleepTime;
    private AtomicBoolean keepRunning;
    private Map<String, Object> streamConfig;
    private Queue<StreamsDatum> inQueue;
    private AtomicBoolean isRunning;

    private DatumStatusCounter statusCounter = new DatumStatusCounter();

    @Override
    public DatumStatusCounter getDatumStatusCounter() {
        return this.statusCounter;
    }


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

            while(this.keepRunning.get()) {
                // The queue is empty, we might as well yield
                // and take a very quick rest
                if(this.inQueue.size() == 0)
                    safeQuickRest();

                StreamsDatum datum = null;
                while((datum = this.inQueue.poll()) != null) {
                    try {
                        this.writer.write(datum);
                        statusCounter.incrementStatus(DatumStatus.SUCCESS);
                    } catch (Exception e) {
                        LOGGER.error("Error writing to persist writer {}", this.writer.getClass().getSimpleName(), e);
                        this.keepRunning.set(false);
                        statusCounter.incrementStatus(DatumStatus.FAIL);
                    }
                }
            }

        } catch(Exception e) {
            LOGGER.error("Failed to execute Persist Writer {}",this.writer.getClass().getSimpleName(), e);
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
