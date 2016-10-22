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

package org.apache.streams.moreover;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Preconditions;
import com.google.common.collect.*;
import com.google.common.util.concurrent.Uninterruptibles;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import com.typesafe.config.ConfigParseOptions;
import org.apache.streams.config.ComponentConfigurator;
import org.apache.streams.config.StreamsConfiguration;
import org.apache.streams.config.StreamsConfigurator;
import org.apache.streams.core.StreamsDatum;
import org.apache.streams.core.StreamsProvider;
import org.apache.streams.core.StreamsResultSet;
import org.apache.streams.jackson.StreamsJacksonMapper;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.math.BigInteger;
import java.util.*;
import java.util.concurrent.*;

/**
 * Streams Provider for the Moreover Metabase API
 *
 *  To use from command line:
 *
 *  Supply configuration similar to src/test/resources/rss.conf
 *
 *  Launch using:
 *
 *  mvn exec:java -Dexec.mainClass=org.apache.streams.moreover.MoreoverProvider -Dexec.args="rss.conf articles.json"
 */
public class MoreoverProvider implements StreamsProvider {

    public final static String STREAMS_ID = "MoreoverProvider";

    private final static Logger LOGGER = LoggerFactory.getLogger(MoreoverProvider.class);

    protected volatile Queue<StreamsDatum> providerQueue = new ConcurrentLinkedQueue<StreamsDatum>();

    private List<MoreoverKeyData> keys;

    private MoreoverConfiguration config;

    private ExecutorService executor;

    public MoreoverProvider(MoreoverConfiguration moreoverConfiguration) {
        this.config = moreoverConfiguration;
        this.keys = Lists.newArrayList();
        for( MoreoverKeyData apiKey : config.getApiKeys()) {
            this.keys.add(apiKey);
        }
    }

    @Override
    public String getId() {
        return STREAMS_ID;
    }

    public void startStream() {

        for(MoreoverKeyData key : keys) {
            MoreoverProviderTask task = new MoreoverProviderTask(key.getId(), key.getKey(), this.providerQueue, key.getStartingSequence());
            executor.submit(new Thread(task));
            LOGGER.info("Started producer for {}", key.getKey());
        }

    }

    @Override
    public synchronized StreamsResultSet readCurrent() {

        LOGGER.debug("readCurrent: {}", providerQueue.size());

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
    public boolean isRunning() {
        return !executor.isShutdown() && !executor.isTerminated();
    }

    @Override
    public void prepare(Object configurationObject) {
        LOGGER.debug("Prepare");
        executor = Executors.newSingleThreadExecutor();
    }

    @Override
    public void cleanUp() {

    }

    public static void main(String[] args) throws Exception {

        Preconditions.checkArgument(args.length >= 2);

        String configfile = args[0];
        String outfile = args[1];

        Config reference = ConfigFactory.load();
        File conf_file = new File(configfile);
        assert(conf_file.exists());
        Config testResourceConfig = ConfigFactory.parseFileAnySyntax(conf_file, ConfigParseOptions.defaults().setAllowMissing(false));

        Config typesafe  = testResourceConfig.withFallback(reference).resolve();

        StreamsConfiguration streamsConfiguration = StreamsConfigurator.detectConfiguration(typesafe);
        MoreoverConfiguration config = new ComponentConfigurator<>(MoreoverConfiguration.class).detectConfiguration(typesafe, "rss");
        MoreoverProvider provider = new MoreoverProvider(config);

        ObjectMapper mapper = StreamsJacksonMapper.getInstance();

        PrintStream outStream = new PrintStream(new BufferedOutputStream(new FileOutputStream(outfile)));
        provider.prepare(config);
        provider.startStream();
        do {
            Uninterruptibles.sleepUninterruptibly(streamsConfiguration.getBatchFrequencyMs(), TimeUnit.MILLISECONDS);
            Iterator<StreamsDatum> iterator = provider.readCurrent().iterator();
            while(iterator.hasNext()) {
                StreamsDatum datum = iterator.next();
                String json;
                try {
                    json = mapper.writeValueAsString(datum.getDocument());
                    outStream.println(json);
                } catch (JsonProcessingException e) {
                    System.err.println(e.getMessage());
                }
            }
        } while( provider.isRunning());
        provider.cleanUp();
        outStream.flush();
    }
}
