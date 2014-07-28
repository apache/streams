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
import com.datasift.client.stream.DeletedInteraction;
import com.datasift.client.stream.Interaction;
import com.datasift.client.stream.StreamEventListener;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import com.google.common.collect.Queues;
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
public class DatasiftPushProvider implements StreamsProvider {

    private final static Logger LOGGER = LoggerFactory.getLogger(DatasiftPushProvider.class);

    private DatasiftConfiguration config;
    protected ConcurrentLinkedQueue<Interaction> interactions = new ConcurrentLinkedQueue<Interaction>();
    private Map<String, DataSiftClient> clients;
    private StreamEventListener eventListener;
    private ObjectMapper mapper;

    public DatasiftPushProvider() {

    }

    // to set up a webhook we need to be able to return a reference to this queue
    public Queue<Interaction> getInteractions() {
        return interactions;
    }

    @Override
    public void startStream() {

        Preconditions.checkNotNull(this.config);
        Preconditions.checkNotNull(this.config.getApiKey());
        Preconditions.checkNotNull(this.config.getUserName());

    }

    /**
     * Shuts down all open streams from datasift.
     */
    public void stop() {
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

    public StreamsResultSet readRange(DateTime start, DateTime end) {
        return null;
    }

    @Override
    public boolean isRunning() {
        return this.clients != null && this.clients.size() > 0;
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
