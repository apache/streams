package org.apache.streams.data.moreover;

import com.google.common.base.Predicates;
import com.google.common.collect.*;
import net.jcip.annotations.Immutable;
import org.apache.streams.core.StreamsDatum;
import org.apache.streams.core.StreamsProvider;
import org.apache.streams.core.StreamsResultSet;
import org.apache.streams.moreover.MoreoverConfiguration;
import org.apache.streams.moreover.MoreoverKeyData;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;
import java.util.*;
import java.util.concurrent.*;

public class MoreoverProvider implements StreamsProvider {

    public final static String STREAMS_ID = "MoreoverProvider";

    private final static Logger LOGGER = LoggerFactory.getLogger(MoreoverProvider.class);

    protected volatile Queue<StreamsDatum> providerQueue = new ConcurrentLinkedQueue<StreamsDatum>();

    private List<MoreoverKeyData> keys;

    private MoreoverConfiguration config;

    private ExecutorService executor;

    public MoreoverProvider(MoreoverConfiguration moreoverConfiguration) {
        this.config = moreoverConfiguration;
        this.keys = Lists.newArrayList();
        for( MoreoverKeyData apiKey : config.getApiKeys()) {
            this.keys.add(apiKey);
        }
    }

    public void startStream() {

        for(MoreoverKeyData key : keys) {
            MoreoverProviderTask task = new MoreoverProviderTask(key.getId(), key.getKey(), this.providerQueue, key.getStartingSequence());
            executor.submit(new Thread(task));
            LOGGER.info("Started producer for {}", key.getKey());
        }

    }

    @Override
    public synchronized StreamsResultSet readCurrent() {
        LOGGER.debug("readCurrent");

        LOGGER.info("Providing {} docs", providerQueue.size());

        Collection<StreamsDatum> currentIterator = Lists.newArrayList();
        Iterators.addAll(currentIterator, providerQueue.iterator());

        StreamsResultSet current = new StreamsResultSet(Queues.newConcurrentLinkedQueue(currentIterator));

        LOGGER.info("Exiting");

        providerQueue.clear();

        return current;
    }

    @Override
    public StreamsResultSet readNew(BigInteger sequence) {
        return null;
    }

    @Override
    public StreamsResultSet readRange(DateTime start, DateTime end) {
        return null;
    }

    @Override
    public void prepare(Object configurationObject) {
        LOGGER.debug("Prepare");
        executor = Executors.newSingleThreadExecutor();
    }

    @Override
    public void cleanUp() {

    }
}
