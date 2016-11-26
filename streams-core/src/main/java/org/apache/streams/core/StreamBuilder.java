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

import org.apache.streams.config.StreamsConfiguration;

import org.joda.time.DateTime;

import java.io.Serializable;
import java.math.BigInteger;

/**
 * Interface for building data streams.
 *
 * <pre>
 *     StreamBuilder builder = ...
 *     builder.newReadCurrentStream(. . .)
 *            .addStreamsProcessor(. . .)
 *            ...
 *            .addStreamsPersistWriter(. . .)
 *     builder.run();
 * </pre>
 *
 */
public interface StreamBuilder extends Serializable {

  StreamBuilder setStreamsConfiguration(StreamsConfiguration configuration);

  StreamsConfiguration getStreamsConfiguration();

  /**
   * Add a {@link org.apache.streams.core.StreamsProcessor} to the data processing stream.
   * @param processorId unique id for this processor - must be unique across the entire stream
   * @param processor the processor to execute
   * @param numTasks the number of instances of this processor to run concurrently
   * @param connectToIds the ids of the {@link org.apache.streams.core.StreamsOperation} that this process will
   *                     receive data from.
   * @return this
   */
  StreamBuilder addStreamsProcessor(String processorId, StreamsProcessor processor, int numTasks, String... connectToIds);

  /**
   * Add a {@link org.apache.streams.core.StreamsPersistWriter} to the data processing stream.
   * @param persistWriterId unique id for this processor - must be unique across the entire stream
   * @param writer the writer to execute
   * @param numTasks the number of instances of this writer to run concurrently
   * @param connectToIds the ids of the {@link org.apache.streams.core.StreamsOperation} that this process will
   *                     receive data from.
   * @return this
   */
  StreamBuilder addStreamsPersistWriter(String persistWriterId, StreamsPersistWriter writer, int numTasks, String... connectToIds);

  /**
   * Add a {@link org.apache.streams.core.StreamsProvider} to the data processing stream.  The provider will execute
   * {@link org.apache.streams.core.StreamsProvider:readCurrent()} to produce data.
   * @param streamId unique if for this provider - must be unique across the entire stream.
   * @param provider provider to execute
   * @return this
   */
  StreamBuilder newPerpetualStream(String streamId, StreamsProvider provider);

  /**
   * Add a {@link org.apache.streams.core.StreamsProvider} to the data processing stream.  The provider will execute
   * {@link org.apache.streams.core.StreamsProvider:readCurrent()} to produce data.
   * @param streamId unique if for this provider - must be unique across the entire stream.
   * @param provider provider to execute
   * @return this
   */
  StreamBuilder newReadCurrentStream(String streamId, StreamsProvider provider);

  /**
   * Add a {@link org.apache.streams.core.StreamsProvider} to the data processing stream.  The provider will execute
   * {@link org.apache.streams.core.StreamsProvider:readNext(BigInteger)} to produce data.
   * @param streamId unique if for this provider - must be unique across the entire stream.
   * @param provider provider to execute
   * @param sequence sequence to pass to {@link org.apache.streams.core.StreamsProvider:readNext(BigInteger)} method
   * @return this
   */
  StreamBuilder newReadNewStream(String streamId, StreamsProvider provider, BigInteger sequence);

  /**
   * Add a {@link org.apache.streams.core.StreamsProvider} to the data processing stream.  The provider will execute
   * {@link org.apache.streams.core.StreamsProvider:readRange(DateTime, DateTime)} to produce data. Whether the start
   * and end dates are inclusive or exclusive is up to the implementation.
   * @param streamId unique if for this provider - must be unique across the entire stream.
   * @param provider provider to execute
   * @param start start date
   * @param end end date
   * @return this
   */
  StreamBuilder newReadRangeStream(String streamId, StreamsProvider provider, DateTime start, DateTime end);

  /**
   * Builds the stream, and starts it or submits it based on implementation.
   */
  void start();

  /**
   * Stops the streams processing.  No guarantee on a smooth shutdown. Optional method, may not be implemented in
   * all cases.
   */
  void stop();

}
