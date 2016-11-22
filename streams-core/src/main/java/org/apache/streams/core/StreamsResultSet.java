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

package org.apache.streams.core;

import java.util.Iterator;
import java.util.Queue;

/**
 * StreamsResultSet is a wrapper for an Iterator around a set of StreamsDatum.
 */
public class StreamsResultSet implements Iterable<StreamsDatum> {

  Queue<StreamsDatum> queue;

  DatumStatusCounter counter;

  public StreamsResultSet(Queue<StreamsDatum> queue) {
    this.queue = queue;
  }


  @Override
  public Iterator<StreamsDatum> iterator() {
    return queue.iterator();
  }

  public int size() {
    return queue.size();
  }

  public Queue<StreamsDatum> getQueue() {
    return queue;
  }

  public void setQueue(Queue<StreamsDatum> queue) {
    this.queue = queue;
  }

  public DatumStatusCounter getCounter() {
    return counter;
  }

  public void setCounter(DatumStatusCounter counter) {
    this.counter = counter;
  }
}

