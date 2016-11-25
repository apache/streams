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

/**
 * A StreamsProvider represents the entry point into the Streams pipeline.  Providers are responsible for inserting
 * data into the pipeline in discrete result sets.
 */
public interface StreamsProvider extends StreamsOperation {

  /**
   * Start the operation of the stream.
   */
  void startStream();

  /**
   * Read the current items available from the provider.
   * @return a non-null {@link org.apache.streams.core.StreamsResultSet}
   */
  StreamsResultSet readCurrent();

  /**
   * Read data with sequenceId greater than sequence.
   * @param sequence BigInteger sequence
   * @return {@link StreamsResultSet}
   */
  StreamsResultSet readNew(BigInteger sequence);

  /**
   * Read data with event time between start DateTime and end DateTime.
   * @param start start DateTime
   * @param end end DateTime
   * @return {@link StreamsResultSet}
   */
  StreamsResultSet readRange(DateTime start, DateTime end);

  /**
   * Flag to indicate whether the provider is still producing data.
   * @return true if the processor is actively awaiting or producing data.  False otherwise.
   */
  boolean isRunning();
}

