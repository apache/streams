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

package org.apache.streams.rss.provider;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.sun.syndication.feed.synd.SyndEntry;
import org.apache.streams.core.StreamsDatum;
import org.apache.streams.pojo.json.Activity;
import org.apache.streams.rss.serializer.SyndEntryActivitySerializer;
import org.apache.streams.rss.serializer.SyndEntrySerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Queue;
import java.util.Random;

public class RssEventProcessor implements Runnable {

    private final static Logger LOGGER = LoggerFactory.getLogger(RssEventProcessor.class);

    private ObjectMapper mapper = new ObjectMapper();

    private Queue<SyndEntry> inQueue;
    private Queue<StreamsDatum> outQueue;

    private Class inClass;
    private Class outClass;

    private SyndEntryActivitySerializer syndEntryActivitySerializer = new SyndEntryActivitySerializer();
    private SyndEntrySerializer syndEntrySerializer = new SyndEntrySerializer();

    public final static String TERMINATE = new String("TERMINATE");

    public RssEventProcessor(Queue<SyndEntry> inQueue, Queue<StreamsDatum> outQueue, Class inClass, Class outClass) {
        this.inQueue = inQueue;
        this.outQueue = outQueue;
        this.inClass = inClass;
        this.outClass = outClass;
    }

    public RssEventProcessor(Queue<SyndEntry> inQueue, Queue<StreamsDatum> outQueue, Class outClass) {
        this.inQueue = inQueue;
        this.outQueue = outQueue;
        this.outClass = outClass;
    }

    @Override
    public void run() {

        while(true) {
            Object item;
            try {
                item = inQueue.poll();
                if(item instanceof String && item.equals(TERMINATE)) {
                    LOGGER.info("Terminating!");
                    break;
                }

                Thread.sleep(new Random().nextInt(100));

                // if the target is string, just pass-through
                if( String.class.equals(outClass))
                    outQueue.offer(new StreamsDatum(item.toString()));
                else if( SyndEntry.class.equals(outClass))
                {
                    outQueue.offer(new StreamsDatum(item));
                }
                else if( Activity.class.equals(outClass))
                {
                    // convert to desired format
                    SyndEntry entry = (SyndEntry)item;
                    if( entry != null ) {
                        Activity out = syndEntryActivitySerializer.deserialize(this.syndEntrySerializer.deserialize((SyndEntry)item));

                        if( out != null )
                            outQueue.offer(new StreamsDatum(out));
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

};
