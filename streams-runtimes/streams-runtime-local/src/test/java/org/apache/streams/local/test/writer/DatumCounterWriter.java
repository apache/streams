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

package org.apache.streams.local.test.writer;

import org.apache.streams.core.StreamsDatum;
import org.apache.streams.core.StreamsPersistWriter;

import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 *
 */
public class DatumCounterWriter implements StreamsPersistWriter{

  @Override
  public String getId() {
    return "DatumCounterWriter";
  }

  /**
   * Set of all ids that have been claimed.  Ensures all instances are assigned unique ids
   */
  public static Set<Integer> CLAIMED_ID = new HashSet<>();
  /**
   * Random instance to generate ids
   */
  public static final Random RAND = new Random();
  /**
   * Set of instance ids that received data. Usefully for testing parrallelization is actually working.
   */
  public final static Set<Integer> SEEN_DATA = Collections.newSetFromMap(new ConcurrentHashMap<Integer, Boolean>());
  /**
   * The total count of data seen by a all instances of a processor.
   */
  public static final ConcurrentHashMap<String, AtomicLong> COUNTS = new ConcurrentHashMap<>();
  /**
   * The documents received
   */
  public static final ConcurrentHashMap<String, List<Object>> RECEIVED = new ConcurrentHashMap<>();

  private int counter = 0;
  private String writerId;
  private Integer id;

  public DatumCounterWriter(String writerId) {
    this.writerId = writerId;
  }

  @Override
  public void write(StreamsDatum entry) {
    ++this.counter;
    SEEN_DATA.add(this.id);
    synchronized (RECEIVED) {
      List<Object> documents = RECEIVED.get(this.writerId);
      if(documents == null) {
        List<Object> docs = new LinkedList<>();
        docs.add(entry.getDocument());
        RECEIVED.put(this.writerId, docs);
      } else {
        documents.add(entry.getDocument());
      }
    }
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
    synchronized (COUNTS) {
      AtomicLong count = COUNTS.get(this.writerId);
      if(count == null) {
        COUNTS.put(this.writerId, new AtomicLong(this.counter));
      } else {
        count.addAndGet(this.counter);
      }
    }
  }

  public int getDatumsCounted() {
    return this.counter;
  }
}
