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
package org.apache.streams.facebook.provider;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Queues;
import com.google.common.collect.Sets;
import com.typesafe.config.ConfigRenderOptions;
import org.apache.streams.config.StreamsConfigurator;
import org.apache.streams.core.StreamsDatum;
import org.apache.streams.core.StreamsProvider;
import org.apache.streams.core.StreamsResultSet;
import org.apache.streams.facebook.FacebookConfiguration;
import org.apache.streams.facebook.IdConfig;
import org.apache.streams.jackson.StreamsJacksonMapper;
import org.apache.streams.util.ComponentUtils;
import org.apache.streams.util.SerializationUtil;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.math.BigInteger;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Abstract {@link org.apache.streams.core.StreamsProvider} for facebook.
 */
public abstract class FacebookProvider implements StreamsProvider {

    private static final Logger LOGGER = LoggerFactory.getLogger(FacebookProvider.class);
    private static final ObjectMapper MAPPER = StreamsJacksonMapper.getInstance();
    private static final int MAX_BATCH_SIZE = 2000;

    protected FacebookConfiguration configuration;
    protected BlockingQueue<StreamsDatum> datums;

    private AtomicBoolean isComplete;
    private ExecutorService executor;
    private FacebookDataCollector dataCollector;


    public FacebookProvider() {
        try {
            this.configuration = MAPPER.readValue(StreamsConfigurator.config.getConfig("facebook").root().render(ConfigRenderOptions.concise()), FacebookConfiguration.class);
        } catch (IOException ioe) {
            LOGGER.error("Exception trying to read default config : {}", ioe);
        }
    }

    public FacebookProvider(FacebookConfiguration configuration) {
        this.configuration = (FacebookConfiguration) SerializationUtil.cloneBySerialization(configuration);
    }


    @Override
    public void startStream() {
        this.dataCollector = getDataCollector();
        this.executor.submit(dataCollector);
    }

    protected abstract FacebookDataCollector getDataCollector();

    @Override
    public StreamsResultSet readCurrent() {
        int batchSize = 0;
        BlockingQueue<StreamsDatum> batch = Queues.newLinkedBlockingQueue();
        while(!this.datums.isEmpty() && batchSize < MAX_BATCH_SIZE) {
            ComponentUtils.offerUntilSuccess(ComponentUtils.pollWhileNotEmpty(this.datums), batch);
            ++batchSize;
        }
        this.isComplete.set(batch.isEmpty() && this.datums.isEmpty() && this.dataCollector.isComplete());
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
        return !this.isComplete.get();
    }

    @Override
    public void prepare(Object configurationObject) {
        this.datums = Queues.newLinkedBlockingQueue();
        this.isComplete = new AtomicBoolean(false);
        this.executor = Executors.newFixedThreadPool(1);
    }

    @Override
    public void cleanUp() {
        ComponentUtils.shutdownExecutor(executor, 5, 5);
        executor = null;
    }

    /**
     * Overrides the ids and addedAfter time in the configuration
     * @param idsToAfterDate
     */
    public void overrideIds(Map<String, DateTime> idsToAfterDate) {
        Set<IdConfig> ids = Sets.newHashSet();
        for(String id : idsToAfterDate.keySet()) {
            IdConfig idConfig = new IdConfig();
            idConfig.setId(id);
            idConfig.setAfterDate(idsToAfterDate.get(id));
            ids.add(idConfig);
        }
        this.configuration.setIds(ids);
    }
}