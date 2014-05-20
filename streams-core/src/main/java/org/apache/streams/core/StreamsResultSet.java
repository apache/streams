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

import javax.validation.constraints.NotNull;

import java.util.Iterator;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * TODO: This needs to be turned into an interface
 */
public class StreamsResultSet implements Iterable<StreamsDatum> {

    private Queue<StreamsDatum> queue;
    private DatumStatusCounter counter;
    private final AtomicBoolean running = new AtomicBoolean(false);

    /**
     * Construct a new Streams Result set and explicitly set whether
     * or not the queue is running. The stream will continue to be
     * checked until the shutdown() method is called
     * @param queue
     * The queue where you'll be providing the datums. Must <i>not</i>
     * be <i>null</i>
     * @param running
     * Whether or not it is explicitly marked as running
     */
    public StreamsResultSet(@NotNull Queue<StreamsDatum> queue, boolean running) {
        this.queue = queue;
        this.running.set(running);
    }

    public StreamsResultSet(@NotNull Queue<StreamsDatum> queue) {
        this(queue, false);
    }

    /**
     * Whether or not this result set is running
     * @return
     * Whether or not this is currently running
     */
    public boolean isRunning() {
        return this.running.get();
    }

    /**
     * Turn off this result set. The queue will be safely exhausted
     * but will let the runtime implementations know this result set
     * will not be receiving any more data.
     */
    public void shutDown() {
        this.running.set(false);
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

