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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.streams.core.StreamsDatum;
import org.apache.streams.pojo.json.Activity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Queue;
import java.util.Random;
import java.util.concurrent.BlockingQueue;

public class GPlusEventProcessor implements Runnable {

    private final static Logger LOGGER = LoggerFactory.getLogger(GPlusEventProcessor.class);

    private ObjectMapper mapper = new ObjectMapper();

    private BlockingQueue<String> inQueue;
    private Queue<StreamsDatum> outQueue;

    private Class inClass;
    private Class outClass;

    private GPlusActivitySerializer gPlusActivitySerializer = new GPlusActivitySerializer();

    public final static String TERMINATE = new String("TERMINATE");

    public GPlusEventProcessor(BlockingQueue<String> inQueue, Queue<StreamsDatum> outQueue, Class inClass, Class outClass) {
        this.inQueue = inQueue;
        this.outQueue = outQueue;
        this.inClass = inClass;
        this.outClass = outClass;
    }

    public GPlusEventProcessor(BlockingQueue<String> inQueue, Queue<StreamsDatum> outQueue, Class outClass) {
        this.inQueue = inQueue;
        this.outQueue = outQueue;
        this.outClass = outClass;
    }

    @Override
    public void run() {

        while(true) {
            try {
                String item = inQueue.take();
                Thread.sleep(new Random().nextInt(100));
                if(item==TERMINATE) {
                    LOGGER.info("Terminating!");
                    break;
                }

                // first check for valid json
                ObjectNode node = (ObjectNode)mapper.readTree(item);

                // if the target is string, just pass-through
                if( String.class.equals(outClass))
                    outQueue.offer(new StreamsDatum(item));
                else {
                    // convert to desired format
                    com.google.api.services.plus.model.Activity gplusActivity = (com.google.api.services.plus.model.Activity)mapper.readValue(item, com.google.api.services.plus.model.Activity.class);

                    Activity streamsActivity = gPlusActivitySerializer.deserialize(gplusActivity);

                    outQueue.offer(new StreamsDatum(streamsActivity));
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

};
