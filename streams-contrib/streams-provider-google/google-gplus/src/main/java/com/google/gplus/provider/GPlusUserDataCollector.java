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

package com.google.gplus.provider;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.services.plus.Plus;
import com.google.api.services.plus.model.Person;
import com.google.gplus.serializer.util.GPlusPersonDeserializer;
import org.apache.streams.core.StreamsDatum;
import org.apache.streams.google.gplus.configuration.UserInfo;
import org.apache.streams.jackson.StreamsJacksonMapper;
import org.apache.streams.util.api.requests.backoff.BackOffStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.BlockingQueue;

/**
 * Collects user profile information for a specific GPlus user
 */
public  class GPlusUserDataCollector extends GPlusDataCollector {

    private static final Logger LOGGER = LoggerFactory.getLogger(GPlusUserDataCollector.class);
    private static final ObjectMapper MAPPER = StreamsJacksonMapper.getInstance();
    private static final int MAX_ATTEMPTS = 5;

    static { //set up Mapper for Person objects
        SimpleModule simpleModule = new SimpleModule();
        simpleModule.addDeserializer(Person.class, new GPlusPersonDeserializer());
        MAPPER.registerModule(simpleModule);
        MAPPER.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    private BackOffStrategy backOffStrategy;
    private Plus gPlus;
    private BlockingQueue<StreamsDatum> datumQueue;
    private UserInfo userInfo;


    public GPlusUserDataCollector(Plus gPlus, BackOffStrategy backOffStrategy, BlockingQueue<StreamsDatum> datumQueue, UserInfo userInfo) {
        this.gPlus = gPlus;
        this.backOffStrategy = backOffStrategy;
        this.datumQueue = datumQueue;
        this.userInfo = userInfo;
    }

    protected void queueUserHistory() {
        try {
            boolean tryAgain = false;
            int attempts = 0;
            com.google.api.services.plus.model.Person person = null;
            do {
                try {
                    person = this.gPlus.people().get(userInfo.getUserId()).execute();
                    this.backOffStrategy.reset();
                    tryAgain = person == null;
                } catch (GoogleJsonResponseException gjre) {
                    tryAgain = backoffAndIdentifyIfRetry(gjre, this.backOffStrategy);
                }
                ++attempts;
            } while(tryAgain && attempts < MAX_ATTEMPTS);
            String json = MAPPER.writeValueAsString(person);
            this.datumQueue.put(new StreamsDatum(json, person.getId()));
        } catch (Throwable t) {
            LOGGER.warn("Unable to pull user data for user={} : {}", userInfo.getUserId(), t);
            if(t instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
        }
    }

    @Override
    public void run() {
        queueUserHistory();
    }



}
