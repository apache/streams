/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
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

package org.apache.streams.pig;

import org.apache.streams.config.StreamsConfiguration;
import org.apache.streams.core.StreamBuilder;
import org.apache.streams.core.StreamsPersistWriter;
import org.apache.streams.core.StreamsProcessor;
import org.apache.streams.core.StreamsProvider;
import org.joda.time.DateTime;

import java.math.BigInteger;

/**
 * Goal is to be able to build a pig workflow using same syntax as other
 * StreamsBuilders
 *
 * Currently implementers must write own pig scripts to use this module
 */
public class StreamsPigBuilder implements StreamBuilder {

    @Override
    public StreamBuilder setStreamsConfiguration(StreamsConfiguration configuration) {
        return null;
    }

    @Override
    public StreamsConfiguration getStreamsConfiguration() {
        return null;
    }

    @Override
    public StreamBuilder addStreamsProcessor(String s, StreamsProcessor streamsProcessor, int i, String... strings) {
        return null;
    }

    @Override
    public StreamBuilder addStreamsPersistWriter(String s, StreamsPersistWriter streamsPersistWriter, int i, String... strings) {
        return null;
    }

    @Override
    public StreamBuilder newPerpetualStream(String s, StreamsProvider streamsProvider) {
        return null;
    }

    @Override
    public StreamBuilder newReadCurrentStream(String s, StreamsProvider streamsProvider) {
        return null;
    }

    @Override
    public StreamBuilder newReadNewStream(String s, StreamsProvider streamsProvider, BigInteger bigInteger) {
        return null;
    }

    @Override
    public StreamBuilder newReadRangeStream(String s, StreamsProvider streamsProvider, DateTime dateTime, DateTime dateTime2) {
        return null;
    }

    @Override
    public void start() {

    }

    @Override
    public void stop() {

    }
}
