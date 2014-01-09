package org.apache.streams.twitter.provider;

import com.google.common.base.Joiner;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import com.twitter.hbc.ClientBuilder;
import com.twitter.hbc.core.Constants;
import com.twitter.hbc.core.endpoint.StatusesFirehoseEndpoint;
import com.twitter.hbc.core.endpoint.StatusesSampleEndpoint;
import com.twitter.hbc.core.endpoint.StreamingEndpoint;
import com.twitter.hbc.core.processor.StringDelimitedProcessor;
import com.twitter.hbc.httpclient.BasicClient;
import com.twitter.hbc.httpclient.auth.Authentication;
import com.twitter.hbc.httpclient.auth.OAuth1;
import com.typesafe.config.Config;
import org.apache.streams.config.StreamsConfigurator;
import org.apache.streams.core.StreamsProvider;
import org.apache.streams.core.StreamsResultSet;
import org.apache.streams.twitter.TwitterStreamConfiguration;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.List;
import java.util.concurrent.*;

/**
 * Created by sblackmon on 12/10/13.
 */
public class TwitterStreamProvider /*extends BaseRichSpout*/ implements StreamsProvider, Serializable, Runnable {

    private final static Logger LOGGER = LoggerFactory.getLogger(TwitterStreamProvider.class);

    private TwitterStreamConfiguration config;

    private Class klass;

    public TwitterStreamConfiguration getConfig() {
        return config;
    }

    public void setConfig(TwitterStreamConfiguration config) {
        this.config = config;
    }

    BlockingQueue<String> inQueue = new LinkedBlockingQueue<String>(10000);

    private StreamingEndpoint endpoint;
    private BasicClient client;

    public BlockingQueue<Object> getOutQueue() {
        return outQueue;
    }

    BlockingQueue<Object> outQueue = new LinkedBlockingQueue<Object>(10000);

    private ListeningExecutorService executor = MoreExecutors.listeningDecorator(newFixedThreadPoolWithQueueSize(5, 20));

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
    }

    public TwitterStreamProvider(TwitterStreamConfiguration config, Class klass) {
        this.config = config;
        this.klass = klass;
    }

    private void setup() {

        Preconditions.checkNotNull(this.klass);

        Preconditions.checkNotNull(config.getOauth().getConsumerKey());
        Preconditions.checkNotNull(config.getOauth().getConsumerSecret());
        Preconditions.checkNotNull(config.getOauth().getAccessToken());
        Preconditions.checkNotNull(config.getOauth().getAccessTokenSecret());

        Preconditions.checkNotNull(config.getEndpoint());
        if(config.getEndpoint().endsWith("sample.json") ) {
            endpoint = new StatusesSampleEndpoint();

            Optional<List<String>> track = Optional.fromNullable(config.getTrack());
            Optional<List<Long>> follow = Optional.fromNullable(config.getFollow());

            if( track.isPresent() ) endpoint.addPostParameter("track", Joiner.on(",").join(track.get()));
            if( follow.isPresent() ) endpoint.addPostParameter("follow", Joiner.on(",").join(follow.get()));
        }
        else if( config.getEndpoint().endsWith("firehose.json"))
            endpoint = new StatusesFirehoseEndpoint();
        else
            return;

        Authentication auth = new OAuth1(config.getOauth().getConsumerKey(),
                config.getOauth().getConsumerSecret(),
                config.getOauth().getAccessToken(),
                config.getOauth().getAccessTokenSecret());

        client = new ClientBuilder()
                .name("apache/streams/streams-contrib/streams-provider-twitter")
                .hosts(Constants.STREAM_HOST)
                .endpoint(endpoint)
                .authentication(auth)
                .processor(new StringDelimitedProcessor(inQueue))
                .build();

    }

    @Override
    public void run() {

        setup();

        for (int i = 0; i < 10; i++) {
            executor.submit(new TwitterEventProcessor(inQueue, outQueue, klass));
        }

        client.connect();

    }

    @Override
    public void start() {

    }

    @Override
    public void stop() {

    }

    @Override
    public StreamsResultSet readCurrent() {
        return null;
    }

    @Override
    public StreamsResultSet readNew(BigInteger sequence) {
        return null;
    }

    @Override
    public StreamsResultSet readRange(DateTime start, DateTime end) {
        return null;
    }
//
//    @Override
//    public void declareOutputFields(OutputFieldsDeclarer outputFieldsDeclarer) {
//        outputFieldsDeclarer.declare(new Fields("document"));
//        outputFieldsDeclarer.declare(new Fields("type"));
//    }
//
//    @Override
//    public void open(Map map, TopologyContext topologyContext, SpoutOutputCollector spoutOutputCollector) {
//        collector = spoutOutputCollector;
//        run();
//    }
//
//    @Override
//    public void nextTuple() {
//        try {
//            collector.emit( new Values(outQueue.take(), klass) );
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
//
//    }

    public class TwitterStreamCloser implements Runnable {

        BlockingQueue<String> queue;

        public TwitterStreamCloser(BlockingQueue<String> queue) {
            this.queue = queue;
        }

        public void run() {
            for (int i = 0; i < 10; i++) {
                queue.add(TwitterEventProcessor.TERMINATE);
            }
        }

    }


}
