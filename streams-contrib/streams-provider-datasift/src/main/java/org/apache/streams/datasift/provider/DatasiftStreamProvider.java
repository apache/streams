/*
Licensed to the Apache Software Foundation (ASF) under one
or more contributor license agreements.  See the NOTICE file
distributed with this work for additional information
regarding copyright ownership.  The ASF licenses this file
to you under the Apache License, Version 2.0 (the
"License"); you may not use this file except in compliance
with the License.  You may obtain a copy of the License at

  http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing,
software distributed under the License is distributed on an
"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
KIND, either express or implied.  See the License for the
specific language governing permissions and limitations
under the License.
*/
package org.apache.streams.datasift.provider;

import com.datasift.client.DataSiftClient;
import com.datasift.client.DataSiftConfig;
import com.datasift.client.core.Stream;
import com.datasift.client.stream.DeletedInteraction;
import com.datasift.client.stream.Interaction;
import com.datasift.client.stream.StreamEventListener;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import com.google.common.collect.Queues;
import com.typesafe.config.Config;
import org.apache.streams.config.StreamsConfigurator;
import org.apache.streams.core.StreamsDatum;
import org.apache.streams.core.StreamsProvider;
import org.apache.streams.core.StreamsResultSet;
import org.apache.streams.datasift.DatasiftConfiguration;
import org.apache.streams.jackson.StreamsJacksonMapper;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Requires Java Version 1.7!
 * {@code DatasiftStreamProvider} is an implementation of the {@link org.apache.streams.core.StreamsProvider} interface.  The provider
 * uses the Datasift java api to make connections. A single provider creates one connection per StreamHash in the configuration.
 */
public class DatasiftStreamProvider implements StreamsProvider {

    private final static Logger LOGGER = LoggerFactory.getLogger(DatasiftStreamProvider.class);

    private DatasiftConfiguration config;
    private ConcurrentLinkedQueue<Interaction> interactions;
    private Map<String, DataSiftClient> clients;
    private StreamEventListener eventListener;
    private ObjectMapper mapper;

    /**
     * Constructor that searches for available configurations
     *
     * @param listener {@link com.datasift.client.stream.StreamEventListener} that handles deletion notices received from twitter.
     */
    public DatasiftStreamProvider(StreamEventListener listener) {
        this(listener, null);
    }

    /**
     * @param listener {@link com.datasift.client.stream.StreamEventListener} that handles deletion notices received from twitter.
     * @param config   Configuration to use
     */
    public DatasiftStreamProvider(StreamEventListener listener, DatasiftConfiguration config) {
        if (config == null) {
            Config datasiftConfig = StreamsConfigurator.config.getConfig("datasift");
            this.config = DatasiftStreamConfigurator.detectConfiguration(datasiftConfig);
        } else {
            this.config = config;
        }
        this.eventListener = listener;
    }

    @Override
    public void startStream() {

        Preconditions.checkNotNull(this.config);
        Preconditions.checkNotNull(this.config.getStreamHash());
        Preconditions.checkNotNull(this.config.getStreamHash().get(0));
        Preconditions.checkNotNull(this.config.getApiKey());
        Preconditions.checkNotNull(this.config.getUserName());
        Preconditions.checkNotNull(this.clients);

        for (String hash : this.config.getStreamHash()) {
            startStreamForHash(hash);
        }

    }

    /**
     * Creates a connection to datasift and starts collection of data from the resulting string.
     *
     * @param streamHash
     */
    public void startStreamForHash(String streamHash) {
        shutDownStream(streamHash);
        DataSiftClient client = getNewClient(this.config.getUserName(), this.config.getApiKey());
        client.liveStream().onStreamEvent(this.eventListener);
        client.liveStream().onError(new ErrorHandler(this, streamHash));

        client.liveStream().subscribe(new Subscription(Stream.fromString(streamHash), this.interactions));
        synchronized (this.clients) {
            this.clients.put(streamHash, client);
        }
    }

    /**
     * Exposed for testing purposes.
     *
     * @param userName
     * @param apiKey
     * @return
     */
    protected DataSiftClient getNewClient(String userName, String apiKey) {
        return new DataSiftClient(new DataSiftConfig(userName, apiKey));
    }


    /**
     * If a stream has been opened for the supplied stream hash, that stream will be shutdown.
     *
     * @param streamHash
     */
    public void shutDownStream(String streamHash) {
        synchronized (clients) {
            if (!this.clients.containsKey(streamHash))
                return;
            DataSiftClient client = this.clients.get(streamHash);
            LOGGER.debug("Shutting down stream for hash: {}", streamHash);
            client.shutdown();
            this.clients.remove(client);
        }
    }

    /**
     * Shuts down all open streams from datasift.
     */
    public void stop() {
        synchronized (clients) {
            for (DataSiftClient client : this.clients.values()) {
                client.shutdown();
            }
        }
    }

    // PRIME EXAMPLE OF WHY WE NEED NEW INTERFACES FOR PROVIDERS
    @Override
    //This is a hack.  It is only like this because of how perpetual streams work at the moment.  Read list server to debate/vote for new interfaces.
    public StreamsResultSet readCurrent() {
        Queue<StreamsDatum> datums = Queues.newConcurrentLinkedQueue();
        StreamsDatum datum = null;
        Interaction interaction;
        while (!this.interactions.isEmpty()) {
            interaction = this.interactions.poll();
            try {
                datum = new StreamsDatum(this.mapper.writeValueAsString(interaction.getData()), interaction.getData().get("interaction").get("id").textValue());
            } catch (JsonProcessingException jpe) {
                LOGGER.error("Exception while converting Interaction to String : {}", jpe);
            }
            if (datum != null) {
                while (!datums.offer(datum)) {
                    Thread.yield();
                }
            }

        }
        return new StreamsResultSet(datums);
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
        this.interactions = new ConcurrentLinkedQueue<Interaction>();
        this.clients = Maps.newHashMap();
        this.mapper = StreamsJacksonMapper.getInstance();
    }

    @Override
    public void cleanUp() {
        stop();
    }

    public DatasiftConfiguration getConfig() {
        return config;
    }

    public void setConfig(DatasiftConfiguration config) {
        this.config = config;
    }


    /**
     * THIS CLASS NEEDS TO BE REPLACED/OVERRIDDEN BY ALL USERS. TWITTERS TERMS OF SERVICE SAYS THAT EVERYONE MUST
     * DELETE TWEETS FROM THEIR DATA STORE IF THEY RECEIVE A DELETE NOTICE.
     */
    public static class DeleteHandler extends StreamEventListener {

        public void onDelete(DeletedInteraction di) {
            //go off and delete the interaction if you have it stored. This is a strict requirement!
            LOGGER.info("DELETED:\n " + di);
        }
    }

}
