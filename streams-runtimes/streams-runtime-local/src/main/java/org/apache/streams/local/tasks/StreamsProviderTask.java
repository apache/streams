package org.apache.streams.local.tasks;

import org.apache.streams.core.*;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 *
 */
public class StreamsProviderTask extends BaseStreamsTask implements DatumStatusCountable {

    private final static Logger LOGGER = LoggerFactory.getLogger(StreamsProviderTask.class);

    @Override
    public DatumStatusCounter getDatumStatusCounter() {
        return this.statusCounter;
    }

    private static enum Type {
        PERPETUAL,
        READ_CURRENT,
        READ_NEW,
        READ_RANGE
    }

    private static final int START = 0;
    private static final int END = 1;

    private static final int DEFAULT_TIMEOUT_MS = 1000000;

    private StreamsProvider provider;
    private AtomicBoolean keepRunning;
    private Type type;
    private BigInteger sequence;
    private DateTime[] dateRange;
    private Map<String, Object> config;
    private AtomicBoolean isRunning;

    private int timeout;
    private int zeros = 0;
    private DatumStatusCounter statusCounter = new DatumStatusCounter();

    /**
     * Constructor for a StreamsProvider to execute {@link org.apache.streams.core.StreamsProvider:readCurrent()}
     *
     * @param provider
     */
    public StreamsProviderTask(StreamsProvider provider, boolean perpetual) {
        this.provider = provider;
        if (perpetual)
            this.type = Type.PERPETUAL;
        else
            this.type = Type.READ_CURRENT;
        this.keepRunning = new AtomicBoolean(true);
        this.isRunning = new AtomicBoolean(true);
        this.timeout = DEFAULT_TIMEOUT_MS;
    }

    /**
     * Constructor for a StreamsProvider to execute {@link org.apache.streams.core.StreamsProvider:readNew(BigInteger)}
     *
     * @param provider
     * @param sequence
     */
    public StreamsProviderTask(StreamsProvider provider, BigInteger sequence) {
        this.provider = provider;
        this.type = Type.READ_NEW;
        this.sequence = sequence;
        this.keepRunning = new AtomicBoolean(true);
        this.isRunning = new AtomicBoolean(true);
        this.timeout = DEFAULT_TIMEOUT_MS;
    }

    /**
     * Constructor for a StreamsProvider to execute {@link org.apache.streams.core.StreamsProvider:readRange(DateTime,DateTime)}
     *
     * @param provider
     * @param start
     * @param end
     */
    public StreamsProviderTask(StreamsProvider provider, DateTime start, DateTime end) {
        this.provider = provider;
        this.type = Type.READ_RANGE;
        this.dateRange = new DateTime[2];
        this.dateRange[START] = start;
        this.dateRange[END] = end;
        this.keepRunning = new AtomicBoolean(true);
        this.isRunning = new AtomicBoolean(true);
        this.timeout = DEFAULT_TIMEOUT_MS;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    @Override
    public void addInputQueue(Queue<StreamsDatum> inputQueue) {
        throw new UnsupportedOperationException(this.getClass().getName() + " does not support method - setInputQueue()");
    }

    @Override
    public void setStreamConfig(Map<String, Object> config) {
        this.config = config;
    }


    @Override
    public void run() {
        // lock, yes, I am running
        this.isRunning.set(true);

        try {
            // TODO allow for configuration objects
            this.provider.prepare(this.config);
            StreamsResultSet resultSet = null;
            switch (this.type) {
                case PERPETUAL:
                    provider.startStream();
                    while (this.keepRunning.get()) {
                        resultSet = provider.readCurrent();
                        if (resultSet.size() == 0)
                            zeros++;
                        else {
                            zeros = 0;
                        }
                        flushResults(resultSet);
                        // the way this works needs to change...
                        if (zeros > (timeout))
                            this.keepRunning.set(false);
                        safeQuickRest();
                    }
                    break;
                case READ_CURRENT:
                    resultSet = this.provider.readCurrent();
                    break;
                case READ_NEW:
                    resultSet = this.provider.readNew(this.sequence);
                    break;
                case READ_RANGE:
                    resultSet = this.provider.readRange(this.dateRange[START], this.dateRange[END]);
                    break;
                default:
                    throw new RuntimeException("Type has not been added to StreamsProviderTask.");
            }
            if(resultSet != null) {
                /**
                 * We keep running while the provider tells us that we are running or we still have
                 * items in queue to be processed.
                 *
                 * IF, the keep running flag is turned to off, then we exit immediately
                 */
                while ((resultSet.isRunning() || resultSet.getQueue().size() > 0) && this.keepRunning.get()) {
                    if(resultSet.getQueue().size() == 0) {
                        // The process is still running, but there is nothing on the queue...
                        // we just need to be patient and wait, we yield the execution and
                        // wait for 1ms to see if anything changes.
                        safeQuickRest();
                    } else {
                        flushResults(resultSet);
                    }
                }
            }
        } catch (Throwable e) {
            LOGGER.error("There was an unknown error while attempting to read from the provider. This provider cannot continue with this exception.");
            LOGGER.error("The stream will continue running, but not with this provider.");
            LOGGER.error("Exception: {}", e);
            this.isRunning.set(false);
        } finally {
            this.provider.cleanUp();
            this.isRunning.set(false);
        }
    }

    @Override
    public boolean isRunning() {
        return this.isRunning.get();
    }

    public void flushResults(StreamsResultSet resultSet) {
        try {
            StreamsDatum datum;
            while ((datum = resultSet.getQueue().poll()) != null) {
                /**
                 * This is meant to be a hard exit from the system. If we are running
                 * and this flag gets set to false, we are to exit immediately and
                 * abandon anything that is in this queue. The remaining processors
                 * will shutdown gracefully once they have depleted their queue
                 */
                if (!this.keepRunning.get())
                    break;

                processNext(datum);
            }
        }
        catch(Throwable e) {
            LOGGER.warn("Unknown problem reading the queue, no datums affected: {}", e.getMessage());
        }
    }

    private void processNext(StreamsDatum datum) {
        try {
            super.addToOutgoingQueue(datum);
            statusCounter.incrementStatus(DatumStatus.SUCCESS);
        } catch (Throwable e) {
            statusCounter.incrementStatus(DatumStatus.FAIL);
        }
    }
}
