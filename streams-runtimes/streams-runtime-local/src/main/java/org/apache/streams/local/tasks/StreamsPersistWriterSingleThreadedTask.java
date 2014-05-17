package org.apache.streams.local.tasks;

import org.apache.streams.core.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 *
 */
public class StreamsPersistWriterSingleThreadedTask extends BaseStreamsTask implements DatumStatusCountable {

    private final static Logger LOGGER = LoggerFactory.getLogger(StreamsPersistWriterSingleThreadedTask.class);

    protected final StreamsPersistWriter writer;
    protected final AtomicBoolean isRunning = new AtomicBoolean(false);
    protected Map<String, Object> streamConfig;
    protected final DatumStatusCounter statusCounter = new DatumStatusCounter();

    /**
     * Default constructor.  Uses default sleep of 500ms when inbound queue is empty.
     * @param writer writer to execute in task
     */
    public StreamsPersistWriterSingleThreadedTask(StreamsPersistWriter writer) {
        this.writer = writer;
    }

    @Override
    public DatumStatusCounter getDatumStatusCounter() {
        return this.statusCounter;
    }


    @Override
    public void setStreamConfig(Map<String, Object> config) {
        this.streamConfig = config;
    }

    @Override
    public boolean isRunning() {
        return this.isRunning.get();
    }

    @Override
    public void run() {
        try {
            this.writer.prepare(this.streamConfig);

            while(this.keepRunning.get()  || super.isDatumAvailable()) {
                // The queue is empty, we might as well yield
                // and take a very quick rest
                if(!isDatumAvailable())
                    safeQuickRest();

                StreamsDatum datum;

                while((datum = pollNextDatum()) != null) {
                    processThisDatum(datum);
                }
            }

        } catch(Exception e) {
            LOGGER.error("Failed to execute Persist Writer {} - {}", this.writer.toString(), e);
        } finally {
            this.writer.cleanUp();
            this.isRunning.set(false);
        }
    }

    protected final void processThisDatum(StreamsDatum datum) {
        // Lock, yes, I am running
        this.isRunning.set(true);

        try {
            this.writer.write(datum);
            statusCounter.incrementStatus(DatumStatus.SUCCESS);
        } catch (Throwable e) {
            LOGGER.error("Error writing to persist writer {}", this.writer.toString(), e);
            statusCounter.incrementStatus(DatumStatus.FAIL);
        }
        finally {
            this.isRunning.set(false);
        }
    }

    @Override
    public void addOutputQueue(Queue<StreamsDatum> outputQueue) {
        throw new UnsupportedOperationException(this.getClass().getName()+" does not support method - setOutputQueue()");
    }
}
