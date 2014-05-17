package org.apache.streams.local.tasks;

import org.apache.streams.core.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 *
 */
public class StreamsProcessorSingleThreadedTask extends BaseStreamsTask implements DatumStatusCountable {

    private final static Logger LOGGER = LoggerFactory.getLogger(StreamsProcessorSingleThreadedTask.class);

    protected final StreamsProcessor processor;
    protected Map<String, Object> streamConfig;
    protected AtomicBoolean isRunning;
    protected final DatumStatusCounter statusCounter = new DatumStatusCounter();

    /**
     * Default constructor, uses default sleep time of 500ms when inbound queue is empty
     * @param processor process to run in task
     */
    public StreamsProcessorSingleThreadedTask(StreamsProcessor processor) {
        this.processor = processor;
        this.isRunning = new AtomicBoolean(true);
    }

    @Override
    public void setStreamConfig(Map<String, Object> config) {
        if(this.streamConfig != null)
            throw new RuntimeException("This variable has already been set, you cannot set it.");
        this.streamConfig = config;
    }

    @Override
    public boolean isRunning() {
        return this.isRunning.get();
    }

    @Override
    public void run() {
        try {
            this.processor.prepare(this.streamConfig);
            while(this.keepRunning.get() || super.isDatumAvailable()) {
                // we don't have anything to do, let's yield
                // and take a quick rest and wait for people to
                // catch up
                if(!isDatumAvailable())
                    safeQuickRest();

                // try to get the output from the processor
                StreamsDatum datum;
                while((datum = super.pollNextDatum()) != null) {
                    processThisDatum(datum);
                }
            }
        } finally {
            // clean everything up
            this.processor.cleanUp();
        }
    }

    protected final void processThisDatum(StreamsDatum datum) {
        // Lock, yes, I am running
        this.isRunning.set(true);

        try {
            // get the outputs from the queue and pass them down the row
            List<StreamsDatum> output = this.processor.process(datum);
            if (output != null)
                for (StreamsDatum outDatum : output)
                    super.addToOutgoingQueue(outDatum);

            this.statusCounter.incrementStatus(DatumStatus.SUCCESS);
        } catch (Throwable e) {
            LOGGER.warn("There was an error processing datum: {}", datum);
            this.statusCounter.incrementStatus(DatumStatus.FAIL);
        }
        finally {
            // release lock, no I am not running
            this.isRunning.set(false);
        }
    }

    public DatumStatusCounter getDatumStatusCounter() {
        return this.statusCounter;
    }

}
