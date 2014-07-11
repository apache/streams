package org.apache.streams.instagram.provider;

import com.google.common.collect.Queues;
import org.apache.streams.config.StreamsConfigurator;
import org.apache.streams.core.StreamsDatum;
import org.apache.streams.core.StreamsProvider;
import org.apache.streams.core.StreamsResultSet;
import org.apache.streams.instagram.InstagramConfigurator;
import org.apache.streams.instagram.InstagramUserInformationConfiguration;
import org.jinstagram.entity.users.feed.MediaFeedData;
import org.joda.time.DateTime;

import java.math.BigInteger;
import java.util.Queue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Instagram {@link org.apache.streams.core.StreamsProvider} that provides the recent media data for a group of users
 */
public class InstagramRecentMediaProvider implements StreamsProvider {

    private InstagramUserInformationConfiguration config;
    private InstagramRecentMediaCollector dataCollector;
    protected Queue<MediaFeedData> mediaFeedQueue; //exposed for testing
    private ExecutorService executorService;
    private volatile boolean isCompleted;

    public InstagramRecentMediaProvider() {
        this(InstagramConfigurator.detectInstagramUserInformationConfiguration(StreamsConfigurator.config.getConfig("instagram")));
    }

    public InstagramRecentMediaProvider(InstagramUserInformationConfiguration config) {
        this.config = config;
        this.mediaFeedQueue = Queues.newConcurrentLinkedQueue();
        this.isCompleted = false;
    }

    @Override
    public void startStream() {
        this.dataCollector = getInstagramRecentMediaCollector();
        this.executorService = Executors.newSingleThreadExecutor();
        this.executorService.submit(this.dataCollector);
    }

    /**
     * EXPOSED FOR TESTING
     * @return
     */
    protected InstagramRecentMediaCollector getInstagramRecentMediaCollector() {
        return new InstagramRecentMediaCollector(this.mediaFeedQueue, this.config);
    }


    @Override
    public StreamsResultSet readCurrent() {
        Queue<StreamsDatum> batch = Queues.newConcurrentLinkedQueue();
        MediaFeedData data = null;
        synchronized (this.mediaFeedQueue) {
            while(!this.mediaFeedQueue.isEmpty()) {
                data = this.mediaFeedQueue.poll();
                batch.add(new StreamsDatum(data, data.getId()));
            }
        }
        this.isCompleted = batch.size() == 0 && this.mediaFeedQueue.isEmpty() && this.dataCollector.isCompleted();
        return new StreamsResultSet(batch);
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
    public boolean isRunning() {
        return !this.isCompleted;
    }

    @Override
    public void prepare(Object configurationObject) {

    }

    @Override
    public void cleanUp() {
        this.executorService.shutdown();
        try {
            this.executorService.awaitTermination(5, TimeUnit.SECONDS);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
        } finally {
            this.executorService = null;
        }
    }
}
