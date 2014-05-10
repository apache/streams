package org.apache.streams.twitter.provider;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
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
import org.apache.commons.lang.NotImplementedException;
import org.apache.streams.config.StreamsConfigurator;
import org.apache.streams.core.*;
import org.apache.streams.twitter.TwitterStreamConfiguration;
import org.apache.streams.twitter.processor.TwitterEventProcessor;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.*;

/**
 * Created by sblackmon on 12/10/13.
 */
public class TwitterStreamProvider implements StreamsProvider, Serializable, DatumStatusCountable {

    public final static String STREAMS_ID = "TwitterStreamProvider";

    private final static Logger LOGGER = LoggerFactory.getLogger(TwitterStreamProvider.class);

    private TwitterStreamConfiguration config;

    public TwitterStreamConfiguration getConfig() {
        return config;
    }

    public void setConfig(TwitterStreamConfiguration config) {
        this.config = config;
    }

    protected BlockingQueue<String> hosebirdQueue;

    protected volatile Queue<StreamsDatum> providerQueue;

    protected Hosts hosebirdHosts;
    protected Authentication auth;
    protected StreamingEndpoint endpoint;
    protected BasicClient client;

    protected ListeningExecutorService executor;

    private DatumStatusCounter countersCurrent = new DatumStatusCounter();
    private DatumStatusCounter countersTotal = new DatumStatusCounter();

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

    @Override
    public void startStream() {

        for (int i = 0; i < 5; i++) {
            executor.submit(new TwitterEventProcessor(hosebirdQueue, providerQueue, String.class));
        }

        new Thread(new TwitterStreamProviderTask(this)).start();

    }

    @Override
    public synchronized StreamsResultSet readCurrent() {

        StreamsResultSet current;

        synchronized( TwitterStreamProvider.class ) {
            current = new StreamsResultSet(Queues.newConcurrentLinkedQueue(providerQueue));
            current.setCounter(new DatumStatusCounter());
            current.getCounter().add(countersCurrent);
            countersTotal.add(countersCurrent);
            countersCurrent = new DatumStatusCounter();
            providerQueue.clear();
        }

        return current;
    }

    @Override
    public StreamsResultSet readNew(BigInteger sequence) {
        throw new NotImplementedException();
    }

    @Override
    public StreamsResultSet readRange(DateTime start, DateTime end)
    {
        throw new NotImplementedException();
    }

    @Override
    public void prepare(Object o) {

        executor = MoreExecutors.listeningDecorator(newFixedThreadPoolWithQueueSize(5, 20));

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

        hosebirdQueue = new LinkedBlockingQueue<String>(1000);
        providerQueue = new LinkedBlockingQueue<StreamsDatum>(1000);

        client = new ClientBuilder()
            .name("apache/streams/streams-contrib/streams-provider-twitter")
            .hosts(hosebirdHosts)
            .endpoint(endpoint)
            .authentication(auth)
            .connectionTimeout(1200000)
            .processor(new StringDelimitedProcessor(hosebirdQueue))
            .build();

    }

    @Override
    public void cleanUp() {
        for (int i = 0; i < 5; i++) {
            hosebirdQueue.add(TwitterEventProcessor.TERMINATE);
        }
    }

    @Override
    public DatumStatusCounter getDatumStatusCounter() {
        return countersTotal;
    }
}
