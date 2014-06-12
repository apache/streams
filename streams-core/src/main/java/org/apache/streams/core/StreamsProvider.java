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

import org.joda.time.DateTime;

import java.math.BigInteger;
import java.util.Queue;

/**
 * A StreamsProvider represents the entry point into the Streams pipeline.  Providers are responsible for inserting
 * data into the pipeline in discrete result sets.
 */
public interface StreamsProvider extends StreamsOperation {

    /**
     * Start the operation of the stream
     */
    void startStream();

    /**
     * Read the current items available from the provider
     * @return a non-null {@link org.apache.streams.core.StreamsResultSet}
     */
    StreamsResultSet readCurrent();

    /**
     * TODO: Define how this operates or eliminate
     * @param sequence
     * @return
     */
    StreamsResultSet readNew(BigInteger sequence);

    /**
     * TODO: Define how this operates or eliminate
     * @param start
     * @param end
     * @return
     */
    StreamsResultSet readRange(DateTime start, DateTime end);

    /**
     * Flag to indicate whether the provider is producing data
     * @return true if the processor is actively awaiting or producing data.  False otherwise.
     */
    boolean isRunning();
}

