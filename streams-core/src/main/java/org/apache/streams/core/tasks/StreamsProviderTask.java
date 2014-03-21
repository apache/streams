package org.apache.streams.core.tasks;

import org.apache.streams.core.StreamsDatum;
import org.apache.streams.core.StreamsProvider;
import org.apache.streams.core.StreamsResultSet;
import org.joda.time.DateTime;

import java.math.BigInteger;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 *
 */
public class StreamsProviderTask extends BaseStreamsTask {

    private static enum Type {
        PERPETUAL,
        READ_CURRENT,
        READ_NEW,
        READ_RANGE
    }

    private static final int START = 0;
    private static final int END = 1;

    private StreamsProvider provider;
    private AtomicBoolean keepRunning;
    private Type type;
    private BigInteger sequence;
    private DateTime[] dateRange;
    private Map<String, Object> config;
    private AtomicBoolean isRunning;

    /**
     * Constructor for a StreamsProvider to execute {@link org.apache.streams.core.StreamsProvider:readCurrent()}
     * @param provider
     */
    public StreamsProviderTask(StreamsProvider provider, boolean perpetual) {
        this.provider = provider;
        if( perpetual )
            this.type = Type.PERPETUAL;
        else
            this.type = Type.READ_CURRENT;
        this.keepRunning = new AtomicBoolean(true);
        this.isRunning = new AtomicBoolean(true);
    }

    /**
     * Constructor for a StreamsProvider to execute {@link org.apache.streams.core.StreamsProvider:readNew(BigInteger)}
     * @param provider
     * @param sequence
     */
    public StreamsProviderTask(StreamsProvider provider, BigInteger sequence) {
        this.provider = provider;
        this.type = Type.READ_NEW;
        this.sequence = sequence;
        this.keepRunning = new AtomicBoolean(true);
        this.isRunning = new AtomicBoolean(true);
    }

    /**
     * Constructor for a StreamsProvider to execute {@link org.apache.streams.core.StreamsProvider:readRange(DateTime,DateTime)}
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
    }

    @Override
    public void stopTask() {
        this.keepRunning.set(false);
    }

    @Override
    public void addInputQueue(Queue<StreamsDatum> inputQueue) {
        throw new UnsupportedOperationException(this.getClass().getName()+" does not support method - setInputQueue()");
    }

    @Override
    public void setStreamConfig(Map<String, Object> config) {
        this.config = config;
    }

    @Override
    public void run() {
        try {
            this.provider.prepare(this.config); //TODO allow for configuration objects
            StreamsResultSet resultSet = null;
            this.isRunning.set(true);
            switch(this.type) {
                case PERPETUAL: {
                    provider.startStream();
                    while(this.keepRunning.get() == true) {
                        try {
                            resultSet = provider.readCurrent();
                            flushResults(resultSet);
                            Thread.sleep(DEFAULT_SLEEP_TIME_MS);
                        } catch (InterruptedException e) {
                            this.keepRunning.set(false);
                        }
                    }
                }
                    break;
                case READ_CURRENT: resultSet = this.provider.readCurrent();
                    break;
                case READ_NEW: resultSet = this.provider.readNew(this.sequence);
                    break;
                case READ_RANGE: resultSet = this.provider.readRange(this.dateRange[START], this.dateRange[END]);
                    break;
                default: throw new RuntimeException("Type has not been added to StreamsProviderTask.");
            }
            flushResults(resultSet);

        } catch( Exception e ) {
            e.printStackTrace();
        } finally
        {
            this.provider.cleanUp();
            this.isRunning.set(false);
        }
    }

    public boolean isRunning() {
        return this.isRunning.get();
    }

    public void flushResults(StreamsResultSet resultSet) {
        for(StreamsDatum datum : resultSet) {
            if(!this.keepRunning.get()) {
                break;
            }
            if(datum != null)
                super.addToOutgoingQueue(datum);
            else {
                try {
                    Thread.sleep(DEFAULT_SLEEP_TIME_MS);
                } catch (InterruptedException e) {
                    this.keepRunning.set(false);
                }
            }
        }
    }
}
