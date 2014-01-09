package org.apache.streams.data.moreover;

import com.google.common.collect.Lists;
import org.apache.streams.core.StreamsProvider;
import org.apache.streams.core.StreamsResultSet;
import org.apache.streams.moreover.MoreoverConfiguration;
import org.apache.streams.moreover.MoreoverKeyData;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.*;

public class MoreoverProvider implements StreamsProvider {

    private static Logger logger = LoggerFactory.getLogger(MoreoverProvider.class);
    private volatile Queue<StreamsResultSet> resultQueue = new ConcurrentLinkedQueue<StreamsResultSet>();
    private List<ExecutorService> tasks = new LinkedList<ExecutorService>();
    private List<MoreoverKeyData> keys;
    private boolean started = false;

    public MoreoverProvider(MoreoverConfiguration moreoverConfiguration) {
        this.keys = Lists.newArrayList();
        for( MoreoverKeyData apiKey : moreoverConfiguration.getApiKeys()) {
            this.keys.add(apiKey);
        }
        this.keys = Arrays.asList();
    }

    public MoreoverProvider(MoreoverKeyData... keys) {
        this.keys = Arrays.asList(keys);
    }

    @Override
    public synchronized void start() {
        logger.trace("Starting Producer");
        if(!started) {
            logger.trace("Producer not started.  Initializing");
            for(MoreoverKeyData key : keys) {
                MoreoverProviderTask task = new MoreoverProviderTask(key.getId(), key.getKey(), this.resultQueue, key.getStartingSequence());
                ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor();
                service.scheduleWithFixedDelay(task, 0, MoreoverProviderTask.LATENCY, TimeUnit.SECONDS);
                logger.info("Started producer for {} with service {}", key.getKey(), service.toString());
                this.tasks.add(service);
            }
            started = true;
        }
    }

    @Override
    public synchronized void stop() {
        for(ExecutorService service: tasks) {
            service.shutdown();
        }
    }

    @Override
    public StreamsResultSet readCurrent() {
        return resultQueue.peek();
    }

    @Override
    public StreamsResultSet readNew(BigInteger sequence) {
        return null;
    }

    @Override
    public StreamsResultSet readRange(DateTime start, DateTime end) {
        return null;
    }

}
