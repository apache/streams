package org.apache.streams.twitter.provider;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import com.typesafe.config.Config;
import org.apache.streams.config.StreamsConfigurator;
import org.apache.streams.core.StreamsDatum;
import org.apache.streams.core.StreamsProvider;
import org.apache.streams.core.StreamsResultSet;
import org.apache.streams.twitter.TwitterStreamConfiguration;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;
import twitter4j.Twitter;
import twitter4j.TwitterFactory;
import twitter4j.conf.ConfigurationBuilder;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.Iterator;
import java.util.Queue;
import java.util.Random;
import java.util.concurrent.*;

/**
 * Created by sblackmon on 12/10/13.
 */
public class TwitterTimelineProvider implements StreamsProvider, Serializable, Runnable {

    private final static Logger LOGGER = LoggerFactory.getLogger(TwitterTimelineProvider.class);

    private TwitterStreamConfiguration config;

    private Class klass;

    public TwitterStreamConfiguration getConfig() {
        return config;
    }

    public void setConfig(TwitterStreamConfiguration config) {
        this.config = config;
    }

    protected volatile BlockingQueue<String> inQueue = new LinkedBlockingQueue<String>(10000);

    protected volatile Queue<StreamsDatum> providerQueue = new LinkedBlockingQueue<StreamsDatum>();

    protected Twitter client;

    ListenableFuture providerTaskComplete;
//
//    public BlockingQueue<Object> getInQueue() {
//        return inQueue;
//    }

    protected ListeningExecutorService executor;

    protected DateTime start;
    protected DateTime end;

    private static ExecutorService newFixedThreadPoolWithQueueSize(int nThreads, int queueSize) {
        return new ThreadPoolExecutor(nThreads, nThreads,
                5000L, TimeUnit.MILLISECONDS,
                new ArrayBlockingQueue<Runnable>(queueSize, true), new ThreadPoolExecutor.CallerRunsPolicy());
    }

    public TwitterTimelineProvider() {
        Config config = StreamsConfigurator.config.getConfig("twitter");
        this.config = TwitterStreamConfigurator.detectConfiguration(config);
    }

    public TwitterTimelineProvider(TwitterStreamConfiguration config) {
        this.config = config;
    }

    public TwitterTimelineProvider(Class klass) {
        Config config = StreamsConfigurator.config.getConfig("twitter");
        this.config = TwitterStreamConfigurator.detectConfiguration(config);
        this.klass = klass;
    }

    public TwitterTimelineProvider(TwitterStreamConfiguration config, Class klass) {
        this.config = config;
        this.klass = klass;
    }

    public Queue<StreamsDatum> getProviderQueue() {
        return this.providerQueue;
    }

    public void run() {

        executor = MoreExecutors.listeningDecorator(newFixedThreadPoolWithQueueSize(5, 20));

        Preconditions.checkNotNull(providerQueue);

        Preconditions.checkNotNull(this.klass);

        Preconditions.checkNotNull(config.getOauth().getConsumerKey());
        Preconditions.checkNotNull(config.getOauth().getConsumerSecret());
        Preconditions.checkNotNull(config.getOauth().getAccessToken());
        Preconditions.checkNotNull(config.getOauth().getAccessTokenSecret());

        Preconditions.checkNotNull(config.getFollow());

        Preconditions.checkArgument(config.getEndpoint().equals("statuses/user_timeline"));

        Boolean jsonStoreEnabled = Optional.fromNullable(new Boolean(Boolean.parseBoolean(config.getJsonStoreEnabled()))).or(true);
        Boolean includeEntitiesEnabled = Optional.fromNullable(new Boolean(Boolean.parseBoolean(config.getIncludeEntities()))).or(true);

        Iterator<Long> ids = config.getFollow().iterator();
        while( ids.hasNext() ) {
            Long id = ids.next();

            String baseUrl = config.getProtocol() + "://" + config.getHost() + ":" + config.getPort() + "/" + config.getVersion() + "/";

            ConfigurationBuilder builder = new ConfigurationBuilder()
                    .setOAuthConsumerKey(config.getOauth().getConsumerKey())
                    .setOAuthConsumerSecret(config.getOauth().getConsumerSecret())
                    .setOAuthAccessToken(config.getOauth().getAccessToken())
                    .setOAuthAccessTokenSecret(config.getOauth().getAccessTokenSecret())
                    .setIncludeEntitiesEnabled(includeEntitiesEnabled)
                    .setJSONStoreEnabled(jsonStoreEnabled)
                    .setAsyncNumThreads(3)
                    .setRestBaseURL(baseUrl);

            Twitter twitter = new TwitterFactory(builder.build()).getInstance();

            providerTaskComplete = executor.submit(new TwitterTimelineProviderTask(this, twitter, id));
        }

        for (int i = 0; i < 1; i++) {
            executor.submit(new TwitterEventProcessor(inQueue, providerQueue, klass));
        }
    }

    @Override
    public StreamsResultSet readCurrent() {
        run();
        StreamsResultSet result = (StreamsResultSet)providerQueue.iterator();
        return result;
    }

    @Override
    public StreamsResultSet readNew(BigInteger sequence) {
        throw new NotImplementedException();
    }

    @Override
    public StreamsResultSet readRange(DateTime start, DateTime end) {
        this.start = start;
        this.end = end;
        run();
        StreamsResultSet result = (StreamsResultSet)providerQueue.iterator();
        return result;
    }

    void shutdownAndAwaitTermination(ExecutorService pool) {
        pool.shutdown(); // Disable new tasks from being submitted
        try {
            // Wait a while for existing tasks to terminate
            if (!pool.awaitTermination(10, TimeUnit.SECONDS)) {
                pool.shutdownNow(); // Cancel currently executing tasks
                // Wait a while for tasks to respond to being cancelled
                if (!pool.awaitTermination(10, TimeUnit.SECONDS))
                    System.err.println("Pool did not terminate");
            }
        } catch (InterruptedException ie) {
            // (Re-)Cancel if current thread also interrupted
            pool.shutdownNow();
            // Preserve interrupt status
            Thread.currentThread().interrupt();
        }
    }


    @Override
    public void prepare(Object o) {

        Preconditions.checkNotNull(providerQueue);

        Preconditions.checkNotNull(this.klass);

        Preconditions.checkNotNull(config.getOauth().getConsumerKey());
        Preconditions.checkNotNull(config.getOauth().getConsumerSecret());
        Preconditions.checkNotNull(config.getOauth().getAccessToken());
        Preconditions.checkNotNull(config.getOauth().getAccessTokenSecret());

        Preconditions.checkNotNull(config.getFollow());

        Preconditions.checkArgument(config.getEndpoint().equals("statuses/user_timeline"));

        Boolean jsonStoreEnabled = Optional.fromNullable(new Boolean(Boolean.parseBoolean(config.getJsonStoreEnabled()))).or(true);
        Boolean includeEntitiesEnabled = Optional.fromNullable(new Boolean(Boolean.parseBoolean(config.getIncludeEntities()))).or(true);

        Iterator<Long> ids = config.getFollow().iterator();
        while( ids.hasNext() ) {
            Long id = ids.next();

            String baseUrl = config.getProtocol() + "://" + config.getHost() + ":" + config.getPort() + "/" + config.getVersion() + "/";

            ConfigurationBuilder builder = new ConfigurationBuilder()
                    .setOAuthConsumerKey(config.getOauth().getConsumerKey())
                    .setOAuthConsumerSecret(config.getOauth().getConsumerSecret())
                    .setOAuthAccessToken(config.getOauth().getAccessToken())
                    .setOAuthAccessTokenSecret(config.getOauth().getAccessTokenSecret())
                    .setIncludeEntitiesEnabled(includeEntitiesEnabled)
                    .setJSONStoreEnabled(jsonStoreEnabled)
                    .setAsyncNumThreads(3)
                    .setRestBaseURL(baseUrl);

            Twitter twitter = new TwitterFactory(builder.build()).getInstance();
            providerTaskComplete = executor.submit(new TwitterTimelineProviderTask(this, twitter, id));
        }

        for (int i = 0; i < 1; i++) {
            executor.submit(new TwitterEventProcessor(inQueue, providerQueue, klass));
        }
    }

    @Override
    public void cleanUp() {
        shutdownAndAwaitTermination(executor);
    }
}
