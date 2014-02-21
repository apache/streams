package org.apache.streams.rss.provider;

import com.google.common.base.Preconditions;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.io.FeedException;
import com.sun.syndication.io.SyndFeedInput;
import com.sun.syndication.io.XmlReader;
import com.typesafe.config.Config;
import org.apache.streams.config.StreamsConfigurator;
import org.apache.streams.core.StreamsDatum;
import org.apache.streams.core.StreamsProvider;
import org.apache.streams.core.StreamsResultSet;
import org.apache.streams.rss.FeedDetails;
import org.apache.streams.rss.RssStreamConfiguration;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.Serializable;
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.*;

/**
 * Created by sblackmon on 12/10/13.
 */
public class RssStreamProvider /*extends BaseRichSpout*/ implements StreamsProvider, Serializable {

    private final static Logger LOGGER = LoggerFactory.getLogger(RssStreamProvider.class);

    private RssStreamConfiguration config;

    private Class klass;

    public RssStreamConfiguration getConfig() {
        return config;
    }

    public void setConfig(RssStreamConfiguration config) {
        this.config = config;
    }

    protected BlockingQueue inQueue = new LinkedBlockingQueue<SyndEntry>(10000);

    protected volatile Queue<StreamsDatum> providerQueue = new ConcurrentLinkedQueue<StreamsDatum>();

    public BlockingQueue<Object> getInQueue() {
        return inQueue;
    }

    protected ListeningExecutorService executor = MoreExecutors.listeningDecorator(newFixedThreadPoolWithQueueSize(100, 100));

    protected List<SyndFeed> feeds;

    private static ExecutorService newFixedThreadPoolWithQueueSize(int nThreads, int queueSize) {
        return new ThreadPoolExecutor(nThreads, nThreads,
                5000L, TimeUnit.MILLISECONDS,
                new ArrayBlockingQueue<Runnable>(queueSize, true), new ThreadPoolExecutor.CallerRunsPolicy());
    }

    public RssStreamProvider() {
        Config config = StreamsConfigurator.config.getConfig("rss");
        this.config = RssStreamConfigurator.detectConfiguration(config);
    }

    public RssStreamProvider(RssStreamConfiguration config) {
        this.config = config;
    }

    public RssStreamProvider(Class klass) {
        Config config = StreamsConfigurator.config.getConfig("rss");
        this.config = RssStreamConfigurator.detectConfiguration(config);
        this.klass = klass;
    }

    public RssStreamProvider(RssStreamConfiguration config, Class klass) {
        this.config = config;
        this.klass = klass;
    }

    @Override
    public void start() {

        Preconditions.checkNotNull(this.klass);

        Preconditions.checkNotNull(config.getFeeds());

        Preconditions.checkNotNull(config.getFeeds().get(0).getUrl());

        for( FeedDetails feedDetails : config.getFeeds()) {

            executor.submit(new RssFeedSetupTask(this, feedDetails));

        }

        for( int i = 0; i < ((config.getFeeds().size() / 5) + 1); i++ )
            executor.submit(new RssEventProcessor(inQueue, providerQueue, klass));

    }

    @Override
    public void stop() {
        for (int i = 0; i < ((config.getFeeds().size() / 5) + 1); i++) {
            inQueue.add(RssEventProcessor.TERMINATE);
        }
    }

    @Override
    public Queue<StreamsDatum> getProviderQueue() {
        return this.providerQueue;
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

    private class RssFeedSetupTask implements Runnable {

        private RssStreamProvider provider;
        private FeedDetails feedDetails;

        private RssFeedSetupTask(RssStreamProvider provider, FeedDetails feedDetails) {
            this.provider = provider;
            this.feedDetails = feedDetails;
        }

        @Override
        public void run() {

            URL feedUrl;
            SyndFeed feed;
            try {
                feedUrl = new URL(feedDetails.getUrl());
                SyndFeedInput input = new SyndFeedInput();
                try {
                    feed = input.build(new XmlReader(feedUrl));
                    executor.submit(new RssStreamProviderTask(provider, feed, feedDetails));
                    LOGGER.info("Connected: " + feedDetails.getUrl());
                } catch (FeedException e) {
                    LOGGER.warn("FeedException: " + feedDetails.getUrl());
                } catch (IOException e) {
                    LOGGER.warn("IOException: " + feedDetails.getUrl());
                }
            } catch (MalformedURLException e) {
                LOGGER.warn("MalformedURLException: " + feedDetails.getUrl());
            }

        }
    }
}
