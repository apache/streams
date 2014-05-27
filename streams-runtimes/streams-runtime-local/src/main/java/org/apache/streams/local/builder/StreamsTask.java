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
package org.apache.streams.local.builder;

import org.apache.streams.core.StreamsDatum;

import java.util.List;
import java.util.Map;
import java.util.Queue;

/**
 * Interface for all task that will be used to execute instances of {@link org.apache.streams.core.StreamsOperation}
 * in local mode.
 */
public interface StreamsTask extends Runnable {

    /**
     * Informs the task to stop. Tasks may or may not try to empty its inbound queue before halting.
     */
    public void stopTask();

    /**
     * Add an input {@link java.util.Queue} for this task.
     * @param inputQueue
     */
    public void addInputQueue(Queue<StreamsDatum> inputQueue);

    /**
     * Add an output {@link java.util.Queue} for this task.
     * @param outputQueue
     */
    public void addOutputQueue(Queue<StreamsDatum> outputQueue);

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
    public List<Queue<StreamsDatum>> getInputQueues();

    /**
     * Returns the output queues that have been set for this task
     * @return list of output queues
     */
    public List<Queue<StreamsDatum>> getOutputQueues();

}
