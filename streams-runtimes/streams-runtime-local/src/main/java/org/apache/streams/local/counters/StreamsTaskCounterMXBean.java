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
package org.apache.streams.local.counters;

/**
 *
 */
public interface StreamsTaskCounterMXBean {

    /**
     * Get the error rate of the streams process calculated by the number of errors not handled by the {@link org.apache.streams.local.tasks.StreamsTask}
     * divided by the number of datums received.
     * @return error rate
     */
    public double getErrorRate();

    /**
     * Get the number of {@link org.apache.streams.core.StreamsDatum}s emitted by the streams process
     * @return number of emitted datums
     */
    public long getNumEmitted();

    /**
     * Get the number of {@link org.apache.streams.core.StreamsDatum}s received by the streams process
     * @return number of received datums
     */
    public long getNumReceived();

    /**
     * Get the number of errors that the process had to catch because the executing Provider/Processor/Writer did not
     * catch and handle the exception
     * @return number of handled errors
     */
    public long getNumUnhandledErrors();



}
