/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package com.google.gmail.provider;

import com.google.common.base.Preconditions;
import com.google.gmail.GMailConfiguration;
import com.googlecode.gmail4j.GmailClient;
import com.googlecode.gmail4j.GmailConnection;
import com.googlecode.gmail4j.http.HttpGmailConnection;
import com.googlecode.gmail4j.javamail.ImapGmailClient;
import com.googlecode.gmail4j.javamail.ImapGmailConnection;
import com.googlecode.gmail4j.rss.RssGmailClient;
import org.apache.streams.config.ComponentConfigurator;
import org.apache.streams.config.StreamsConfigurator;
import org.apache.streams.core.DatumStatusCounter;
import org.apache.streams.core.StreamsDatum;
import org.apache.streams.core.StreamsProvider;
import org.apache.streams.core.StreamsResultSet;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by sblackmon on 12/10/13.
 */
public class GMailProvider implements StreamsProvider, Serializable {

    public final static String STREAMS_ID = "GMailProvider";

    private final static Logger LOGGER = LoggerFactory.getLogger(GMailProvider.class);

    private GMailConfiguration config;

    private Class klass;

    public GMailConfiguration getConfig() {
        return config;
    }

    public void setConfig(GMailConfiguration config) {
        this.config = config;
    }

    protected BlockingQueue inQueue = new LinkedBlockingQueue<>(10000);

    protected volatile Queue<StreamsDatum> providerQueue = new ConcurrentLinkedQueue<>();
    protected Future task;

    public BlockingQueue<Object> getInQueue() {
        return inQueue;
    }

    protected GmailClient rssClient;
    protected ImapGmailClient imapClient;

    private ExecutorService executor;

    private static ExecutorService newFixedThreadPoolWithQueueSize(int nThreads, int queueSize) {
        return new ThreadPoolExecutor(nThreads, nThreads,
                5000L, TimeUnit.MILLISECONDS,
                new ArrayBlockingQueue<Runnable>(queueSize, true), new ThreadPoolExecutor.CallerRunsPolicy());
    }

    public GMailProvider() {
        this.config = new ComponentConfigurator<>(GMailConfiguration.class)
          .detectConfiguration(StreamsConfigurator.getConfig().getConfig("gmail"));
    }

    public GMailProvider(GMailConfiguration config) {
        this.config = config;
    }

    public GMailProvider(Class klass) {
        this.config = new ComponentConfigurator<>(GMailConfiguration.class)
          .detectConfiguration(StreamsConfigurator.getConfig().getConfig("gmail"));
        this.klass = klass;
    }

    public GMailProvider(GMailConfiguration config, Class klass) {
        this.config = config;
        this.klass = klass;
    }

    protected DatumStatusCounter countersTotal = new DatumStatusCounter();
    protected DatumStatusCounter countersCurrent = new DatumStatusCounter();

    @Override
    public String getId() {
        return "GMailProvider";
    }

    @Override
    public void startStream() {

        task = executor.submit(new GMailImapProviderTask(this));

    }

    @Override
    public StreamsResultSet readCurrent() {

        StreamsResultSet current;

        synchronized( GMailProvider.class ) {
            current = new StreamsResultSet(new ConcurrentLinkedQueue<>(providerQueue));
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
        return null;
    }

    @Override
    public StreamsResultSet readRange(DateTime start, DateTime end) {
        return null;
    }

    @Override
    public boolean isRunning() {
        return !task.isDone() && !task.isCancelled();
    }

    @Override
    public void prepare(Object configurationObject) {

        Preconditions.checkNotNull(this.klass);

        Preconditions.checkNotNull(config.getUserName());
        Preconditions.checkNotNull(config.getPassword());

        rssClient = new RssGmailClient();
        GmailConnection rssConnection = new HttpGmailConnection(config.getUserName(), config.getPassword().toCharArray());
        rssClient.setConnection(rssConnection);

        imapClient = new ImapGmailClient();
        GmailConnection imapConnection = new ImapGmailConnection();
        imapConnection.setLoginCredentials(config.getUserName(), config.getPassword().toCharArray());
        imapClient.setConnection(imapConnection);

        executor = Executors.newSingleThreadExecutor();

        startStream();
    }

    @Override
    public void cleanUp() {
        try {
            executor.awaitTermination(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
