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

package org.apache.streams.local.tasks;

import org.apache.streams.core.StreamsDatum;
import org.apache.streams.local.counters.StreamsTaskCounter;

import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;

/**
 * Interface for all task that will be used to execute instances of {@link org.apache.streams.core.StreamsOperation}
 * in local mode.
 */
public interface StreamsTask extends Runnable{
    /**
     * Represents the default time to sleep a task before polling for the next set of work items
     */
    static final long DEFAULT_SLEEP_TIME_MS = 5000;

    /**
     * Represents the default amount of time to wait for new data to flow through the system before assuming it should
     * shut down
     */
    static final int DEFAULT_TIMEOUT_MS = 100000; //Will result in a default timeout of 1 minute if used

    /**
     * Informs the task to stop. Tasks may or may not try to empty its inbound queue before halting.
     */
    public void stopTask();

    /**
     * Returns true if the task is waiting on more data to process
     * @return true, if waiting on more data to process
     */
    public boolean isWaiting();
    /**
     * Add an input {@link java.util.Queue} for this task.
     * @param inputQueue
     */
    public void addInputQueue(BlockingQueue<StreamsDatum> inputQueue);

    /**
     * Add an output {@link java.util.Queue} for this task.
     * @param outputQueue
     */
    public void addOutputQueue(BlockingQueue<StreamsDatum> outputQueue);

    /**
     * Set the configuration object that will shared and passed to all instances of StreamsTask.
     * @param config optional configuration information
     */
    public void setStreamConfig(Map<String, Object> config);

    /**
     * Returns true when the task has not completed. Returns false otherwise
     * @return true when the task has not completed. Returns false otherwise
     */
    public boolean isRunning();

    /**
     * Returns the input queues that have been set for this task.
     * @return list of input queues
     */
    public List<BlockingQueue<StreamsDatum>> getInputQueues();

    /**
     * Returns the output queues that have been set for this task
     * @return list of output queues
     */
    public List<BlockingQueue<StreamsDatum>> getOutputQueues();


    public void setStreamsTaskCounter(StreamsTaskCounter counter);

}
