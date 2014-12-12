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

import com.datasift.client.stream.DeletedInteraction;
import com.datasift.client.stream.StreamEventListener;
import com.google.common.base.Preconditions;
import com.google.common.collect.Queues;
import org.apache.streams.core.StreamsDatum;
import org.apache.streams.core.StreamsProvider;
import org.apache.streams.core.StreamsResultSet;
import org.apache.streams.datasift.DatasiftConfiguration;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;
import java.util.Queue;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Requires Java Version 1.7!
 * {@code DatasiftStreamProvider} is an implementation of the {@link org.apache.streams.core.StreamsProvider} interface.  The provider
 * uses the Datasift java api to make connections. A single provider creates one connection per StreamHash in the configuration.
 */
public class DatasiftPushProvider implements StreamsProvider {

    private final static Logger LOGGER = LoggerFactory.getLogger(DatasiftPushProvider.class);

    private DatasiftConfiguration config;
    protected Queue<StreamsDatum> providerQueue;

    protected final ReadWriteLock lock = new ReentrantReadWriteLock();

    public DatasiftPushProvider() {

    }

    @Override
    public void startStream() {
        Preconditions.checkNotNull(providerQueue);
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

        StreamsResultSet current = new StreamsResultSet(datums);
        try {
            lock.writeLock().lock();
            current = new StreamsResultSet(providerQueue);
            providerQueue = constructQueue();
        } finally {
            lock.writeLock().unlock();
        }

        return current;
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
        return true;
    }

    @Override
    public void prepare(Object configurationObject) {
        this.providerQueue = constructQueue();
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

    private Queue<StreamsDatum> constructQueue() {
        return Queues.newConcurrentLinkedQueue();
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
