package org.apache.streams.twitter.provider;

import com.google.common.base.Preconditions;
import com.google.common.collect.Queues;
import com.typesafe.config.Config;
import org.apache.streams.config.StreamsConfigurator;
import org.apache.streams.core.DatumStatusCounter;
import org.apache.streams.core.StreamsDatum;
import org.apache.streams.core.StreamsResultSet;
import org.apache.streams.twitter.TwitterUserInformationConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import twitter4j.Twitter;

import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Created by sblackmon on 11/25/14.
 */
public class TwitterFollowingProvider extends TwitterUserInformationProvider {

    public static final String STREAMS_ID = "TwitterFollowingProvider";
    private static final Logger LOGGER = LoggerFactory.getLogger(TwitterFollowingProvider.class);

    protected final ReadWriteLock lock = new ReentrantReadWriteLock();

    public static final int MAX_NUMBER_WAITING = 10000;

    public TwitterFollowingProvider() {
        super(TwitterConfigurator.detectTwitterUserInformationConfiguration(StreamsConfigurator.config.getConfig("twitter")));
    }

    public TwitterFollowingProvider(TwitterUserInformationConfiguration config) {
        super(config);
    }

    @Override
    public void startStream() {

        running.set(true);

        Preconditions.checkArgument(idsBatches.hasNext());

        LOGGER.info("startStream");

        while (idsBatches.hasNext()) {
            submitFollowingThreads(idsBatches.next());
        }

        running.set(true);

        executor.shutdown();

    }

    protected void submitFollowingThreads(Long[] ids) {
        Twitter client = getTwitterClient();

        if( getConfig().getEndpoint().equals("friends") ) {
            for (int i = 0; i < ids.length; i++) {
                TwitterFriendsProviderTask providerTask = new TwitterFriendsProviderTask(this, client, ids[i]);
                executor.submit(providerTask);
            }
        } else if( getConfig().getEndpoint().equals("followers") ) {
            for (int i = 0; i < ids.length; i++) {
                TwitterFollowersProviderTask providerTask = new TwitterFollowersProviderTask(this, client, ids[i]);
                executor.submit(providerTask);
            }
        }
    }

    @Override
    public StreamsResultSet readCurrent() {

        LOGGER.info("Providing {} docs", providerQueue.size());

        StreamsResultSet result;

        try {
            lock.writeLock().lock();
            result = new StreamsResultSet(providerQueue);
            result.setCounter(new DatumStatusCounter());
            providerQueue = constructQueue();
        } finally {
            lock.writeLock().unlock();
        }

        if (providerQueue.isEmpty() && executor.isTerminated()) {
            LOGGER.info("Finished.  Cleaning up...");

            running.set(false);

            LOGGER.info("Exiting");
        }

        return result;

    }

    protected Queue<StreamsDatum> constructQueue() {
        return Queues.synchronizedQueue(new LinkedBlockingQueue<StreamsDatum>(MAX_NUMBER_WAITING));
    }

    @Override
    public void prepare(Object o) {
        super.prepare(o);
        Preconditions.checkNotNull(getConfig().getEndpoint());
        Preconditions.checkArgument(getConfig().getEndpoint().equals("friends") || getConfig().getEndpoint().equals("followers"));
        return;
    }
}
