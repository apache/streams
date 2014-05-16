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

    private final StreamsPersistWriter writer;
    private final AtomicBoolean isRunning = new AtomicBoolean(true);
    private Map<String, Object> streamConfig;

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
        this.writer = writer;
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

            while(this.keepRunning.get()) {
                // The queue is empty, we might as well yield
                // and take a very quick rest
                if(!isDatumAvailable())
                    safeQuickRest();

                StreamsDatum datum = null;

                while((datum = pollNextDatum()) != null) {
                    try {
                        this.writer.write(datum);
                        statusCounter.incrementStatus(DatumStatus.SUCCESS);
                    } catch (Exception e) {
                        LOGGER.error("Error writing to persist writer {} - {}", this.writer.toString(), e);
                        this.keepRunning.set(false);
                        statusCounter.incrementStatus(DatumStatus.FAIL);
                    }
                }
            }

        } catch(Exception e) {
            LOGGER.error("Failed to execute Persist Writer {} - {}", this.writer.toString(), e);
        } finally {
            this.writer.cleanUp();
            this.isRunning.set(false);
        }
    }

    @Override
    public void addOutputQueue(Queue<StreamsDatum> outputQueue) {
        throw new UnsupportedOperationException(this.getClass().getName()+" does not support method - setOutputQueue()");
    }
}
