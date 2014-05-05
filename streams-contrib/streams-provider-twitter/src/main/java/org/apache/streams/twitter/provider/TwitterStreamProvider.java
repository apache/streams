package org.apache.streams.twitter.provider;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import com.google.common.collect.Queues;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import com.twitter.hbc.ClientBuilder;
import com.twitter.hbc.core.Constants;
import com.twitter.hbc.core.Hosts;
import com.twitter.hbc.core.HttpHosts;
import com.twitter.hbc.core.endpoint.*;
import com.twitter.hbc.core.processor.StringDelimitedProcessor;
import com.twitter.hbc.httpclient.BasicClient;
import com.twitter.hbc.httpclient.auth.Authentication;
import com.twitter.hbc.httpclient.auth.BasicAuth;
import com.twitter.hbc.httpclient.auth.OAuth1;
import com.typesafe.config.Config;
import org.apache.streams.config.StreamsConfigurator;
import org.apache.streams.core.StreamsDatum;
import org.apache.streams.core.StreamsProvider;
import org.apache.streams.core.StreamsResultSet;
import org.apache.streams.twitter.TwitterStreamConfiguration;
import org.apache.streams.twitter.processor.TwitterEventProcessor;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.Collection;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.*;

/**
 * Created by sblackmon on 12/10/13.
 */
public class TwitterStreamProvider implements StreamsProvider, Serializable {

    public final static String STREAMS_ID = "TwitterStreamProvider";

    private final static Logger LOGGER = LoggerFactory.getLogger(TwitterStreamProvider.class);

    private TwitterStreamConfiguration config;

    private Class klass;

    public TwitterStreamConfiguration getConfig() {
        return config;
    }

    public void setConfig(TwitterStreamConfiguration config) {
        this.config = config;
    }

    protected BlockingQueue inQueue = new LinkedBlockingQueue<String>(10000);

    protected volatile Queue<StreamsDatum> providerQueue;

    protected Hosts hosebirdHosts;
    protected Authentication auth;
    protected StreamingEndpoint endpoint;
    protected BasicClient client;

    protected ListeningExecutorService executor;

    private static ExecutorService newFixedThreadPoolWithQueueSize(int nThreads, int queueSize) {
        return new ThreadPoolExecutor(nThreads, nThreads,
                5000L, TimeUnit.MILLISECONDS,
                new ArrayBlockingQueue<Runnable>(queueSize, true), new ThreadPoolExecutor.CallerRunsPolicy());
    }

    public TwitterStreamProvider() {
        Config config = StreamsConfigurator.config.getConfig("twitter");
        this.config = TwitterStreamConfigurator.detectConfiguration(config);
    }

    public TwitterStreamProvider(TwitterStreamConfiguration config) {
        this.config = config;
    }

    public TwitterStreamProvider(Class klass) {
        Config config = StreamsConfigurator.config.getConfig("twitter");
        this.config = TwitterStreamConfigurator.detectConfiguration(config);
        this.klass = klass;
        providerQueue = new LinkedBlockingQueue<StreamsDatum>();
    }

    public TwitterStreamProvider(TwitterStreamConfiguration config, Class klass) {
        this.config = config;
        this.klass = klass;
        providerQueue = new LinkedBlockingQueue<StreamsDatum>();

    }

    @Override
    public void startStream() {

        for (int i = 0; i < 5; i++) {
            executor.submit(new TwitterEventProcessor(inQueue, providerQueue, klass));
        }

        new Thread(new TwitterStreamProviderTask(this)).start();
    }

    @Override
    public synchronized StreamsResultSet readCurrent() {
        Collection<StreamsDatum> currentIterator = Lists.newArrayList();
        Iterators.addAll(currentIterator, providerQueue.iterator());

        StreamsResultSet current = new StreamsResultSet(Queues.newConcurrentLinkedQueue(currentIterator));

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
    public void prepare(Object o) {

        executor = MoreExecutors.listeningDecorator(newFixedThreadPoolWithQueueSize(5, 20));

        Preconditions.checkNotNull(this.klass);

        Preconditions.checkNotNull(config.getEndpoint());

        if(config.getEndpoint().equals("userstream") ) {

            hosebirdHosts = new HttpHosts(Constants.USERSTREAM_HOST);

            UserstreamEndpoint userstreamEndpoint = new UserstreamEndpoint();
            userstreamEndpoint.withFollowings(true);
            userstreamEndpoint.withUser(false);
            userstreamEndpoint.allReplies(false);
            endpoint = userstreamEndpoint;
        }
        else if(config.getEndpoint().equals("sample") ) {

            hosebirdHosts = new HttpHosts(Constants.STREAM_HOST);

            Optional<List<String>> track = Optional.fromNullable(config.getTrack());
            Optional<List<Long>> follow = Optional.fromNullable(config.getFollow());

            if( track.isPresent() || follow.isPresent() ) {
                LOGGER.debug("***\tPRESENT\t***");
                StatusesFilterEndpoint statusesFilterEndpoint = new StatusesFilterEndpoint();
                if( track.isPresent() ) {
                    statusesFilterEndpoint.trackTerms(track.get());
                }
                else {
                    statusesFilterEndpoint.followings(follow.get());
                }
                this.endpoint = statusesFilterEndpoint;
            } else {
                endpoint = new StatusesSampleEndpoint();
            }

        }
        else if( config.getEndpoint().endsWith("firehose")) {
            hosebirdHosts = new HttpHosts(Constants.STREAM_HOST);
            endpoint = new StatusesFirehoseEndpoint();
        } else {
            LOGGER.error("NO ENDPOINT RESOLVED");
            return;
        }

        if( config.getBasicauth() != null ) {

            Preconditions.checkNotNull(config.getBasicauth().getUsername());
            Preconditions.checkNotNull(config.getBasicauth().getPassword());

            auth = new BasicAuth(
                    config.getBasicauth().getUsername(),
                    config.getBasicauth().getPassword()
            );

        } else if( config.getOauth() != null ) {

            Preconditions.checkNotNull(config.getOauth().getConsumerKey());
            Preconditions.checkNotNull(config.getOauth().getConsumerSecret());
            Preconditions.checkNotNull(config.getOauth().getAccessToken());
            Preconditions.checkNotNull(config.getOauth().getAccessTokenSecret());

            auth = new OAuth1(config.getOauth().getConsumerKey(),
                    config.getOauth().getConsumerSecret(),
                    config.getOauth().getAccessToken(),
                    config.getOauth().getAccessTokenSecret());

        } else {
            LOGGER.error("NO AUTH RESOLVED");
            return;
        }

        LOGGER.debug("host={}\tendpoint={}\taut={}", new Object[] {hosebirdHosts,endpoint,auth});

        client = new ClientBuilder()
            .name("apache/streams/streams-contrib/streams-provider-twitter")
            .hosts(hosebirdHosts)
            .endpoint(endpoint)
            .authentication(auth)
            .processor(new StringDelimitedProcessor(inQueue))
            .build();

    }

    @Override
    public void cleanUp() {
        for (int i = 0; i < 5; i++) {
            inQueue.add(TwitterEventProcessor.TERMINATE);
        }
    }
}
