package org.apache.streams.datasift.provider;

/*
 * #%L
 * streams-provider-datasift
 * %%
 * Copyright (C) 2013 - 2014 Apache Streams Project
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import com.datasift.client.DataSiftClient;
import com.datasift.client.DataSiftConfig;
import com.datasift.client.core.Stream;
import com.datasift.client.stream.*;
import com.google.common.base.Preconditions;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import com.typesafe.config.Config;
import org.apache.streams.config.StreamsConfigurator;
import org.apache.streams.core.StreamsDatum;
import org.apache.streams.core.StreamsProvider;
import org.apache.streams.core.StreamsResultSet;
import org.apache.streams.datasift.DatasiftConfiguration;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by sblackmon on 12/10/13.
 */
public class DatasiftStreamProvider implements StreamsProvider {

    private final static Logger LOGGER = LoggerFactory.getLogger(DatasiftStreamProvider.class);

    protected DatasiftConfiguration config = null;

    protected DataSiftClient client;

    private Class klass;

    public DatasiftConfiguration getConfig() {
        return config;
    }

    public void setConfig(DatasiftConfiguration config) {
        this.config = config;
    }

    protected BlockingQueue inQueue = new LinkedBlockingQueue<Interaction>(10000);

    protected volatile Queue<StreamsDatum> providerQueue = new ConcurrentLinkedQueue<StreamsDatum>();

    public BlockingQueue<Object> getInQueue() {
        return inQueue;
    }

    protected ListeningExecutorService executor = MoreExecutors.listeningDecorator(newFixedThreadPoolWithQueueSize(100, 100));

    protected List<String> streamHashes;

    private static ExecutorService newFixedThreadPoolWithQueueSize(int nThreads, int queueSize) {
        return new ThreadPoolExecutor(nThreads, nThreads,
                5000L, TimeUnit.MILLISECONDS,
                new ArrayBlockingQueue<Runnable>(queueSize, true), new ThreadPoolExecutor.CallerRunsPolicy());
    }

    public DatasiftStreamProvider() {
        Config datasiftConfig = StreamsConfigurator.config.getConfig("datasift");
        this.config = DatasiftStreamConfigurator.detectConfiguration(datasiftConfig);
    }

    public DatasiftStreamProvider(DatasiftConfiguration config) {
        this.config = config;
    }

    public DatasiftStreamProvider(Class klass) {
        Config config = StreamsConfigurator.config.getConfig("datasift");
        this.config = DatasiftStreamConfigurator.detectConfiguration(config);
        this.klass = klass;
    }

    public DatasiftStreamProvider(DatasiftConfiguration config, Class klass) {
        this.config = config;
        this.klass = klass;
    }

    @Override
    public void startStream() {

        Preconditions.checkNotNull(this.klass);

        Preconditions.checkNotNull(config);

        Preconditions.checkNotNull(config.getStreamHash());

        Preconditions.checkNotNull(config.getStreamHash().get(0));

        for( String hash : config.getStreamHash()) {

            client.liveStream().subscribe(new Subscription(Stream.fromString(hash)));

        }

        for( int i = 0; i < ((config.getStreamHash().size() / 5) + 1); i++ )
            executor.submit(new DatasiftEventProcessor(inQueue, providerQueue, klass));

    }

    public void stop() {

        for( String hash : config.getStreamHash()) {

            client.liveStream().subscribe(new Subscription(Stream.fromString(hash)));

        }
    }

    public Queue<StreamsDatum> getProviderQueue() {
        return this.providerQueue;
    }

    @Override
    public StreamsResultSet readCurrent() {

        return (StreamsResultSet) providerQueue;

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

        Preconditions.checkNotNull(config);

        String apiKey = config.getApiKey();
        String userName = config.getUserName();

        DataSiftConfig config = new DataSiftConfig(userName, apiKey);

        client = new DataSiftClient(config);

        client.liveStream().onError(new ErrorHandler());

        //handle delete message
        client.liveStream().onStreamEvent(new DeleteHandler());

    }

    @Override
    public void cleanUp() {
        stop();
    }

    public class Subscription extends StreamSubscription {
        AtomicLong count = new AtomicLong();

        public Subscription(Stream stream) {
            super(stream);
        }

        public void onDataSiftLogMessage(DataSiftMessage di) {
            //di.isWarning() is also available
            System.out.println((di.isError() ? "Error" : di.isInfo() ? "Info" : "Warning") + ":\n" + di);
        }

        public void onMessage(Interaction i) {

            LOGGER.debug("Processing:\n" + i);

            inQueue.offer(i);

            if (count.incrementAndGet() % 1000 == 0) {
                LOGGER.info("Processed {}:\n " + count.get());

            }

        }
    }

    public class DeleteHandler extends StreamEventListener {
        public void onDelete(DeletedInteraction di) {
            //go off and delete the interaction if you have it stored. This is a strict requirement!
            LOGGER.info("DELETED:\n " + di);
        }
    }

    public class ErrorHandler extends ErrorListener {
        public void exceptionCaught(Throwable t) {
            LOGGER.warn(t.getMessage());
            //do something useful...
        }
    }

}
