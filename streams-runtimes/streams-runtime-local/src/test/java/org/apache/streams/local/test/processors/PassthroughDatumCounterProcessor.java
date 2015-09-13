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

package org.apache.streams.local.test.processors;

import org.apache.streams.core.StreamsDatum;
import org.apache.streams.core.StreamsProcessor;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 *
 */
public class PassthroughDatumCounterProcessor implements StreamsProcessor {

    public final static String STREAMS_ID = "PassthroughDatumCounterProcessor";

    /**
     * Set of all ids that have been claimed.  Ensures all instances are assigned unique ids
     */
    public static Set<Integer> CLAIMED_ID = new HashSet<Integer>();
    /**
     * Random instance to generate ids
     */
    public static final Random RAND = new Random();
    /**
     * Set of instance ids that received data. Usefully for testing parrallelization is actually working.
     */
    public final static Set<Integer> SEEN_DATA = new HashSet<Integer>();
    /**
     * The total count of data seen by a all instances of a processor.
     */
    public static final ConcurrentHashMap<String, AtomicLong> COUNTS = new ConcurrentHashMap<>();

    private int count = 0;
    private int id;
    private String procId;

    public PassthroughDatumCounterProcessor(String procId) {
        this.procId = procId;
    }

    @Override
    public String getId() {
        return STREAMS_ID;
    }

    @Override
    public List<StreamsDatum> process(StreamsDatum entry) {
        ++this.count;
        List<StreamsDatum> result = new LinkedList<StreamsDatum>();
        result.add(entry);
        synchronized (SEEN_DATA) {
            SEEN_DATA.add(this.id);
        }
        return result;
    }

    @Override
    public void prepare(Object configurationObject) {
        synchronized (CLAIMED_ID) {
            this.id = RAND.nextInt();
            while(!CLAIMED_ID.add(this.id)) {
                this.id = RAND.nextInt();
            }
        }
    }

    @Override
    public void cleanUp() {
        System.out.println("Clean up "+this.procId);
        synchronized (COUNTS) {
            AtomicLong count = COUNTS.get(this.procId);
            if(count == null) {
                COUNTS.put(this.procId, new AtomicLong(this.count));
            } else {
                count.addAndGet(this.count);
            }
        }
        System.out.println(this.procId+"\t"+this.count);
    }

    public int getMessageCount() {
        return this.count;
    }
}
