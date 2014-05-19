package org.apache.streams.pig;

/*
 * #%L
 * streams-runtime-pig
 * %%
 * Copyright (C) 2013 - 2014 Apache Streams Project
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import org.apache.streams.core.StreamBuilder;
import org.apache.streams.core.StreamsPersistWriter;
import org.apache.streams.core.StreamsProcessor;
import org.apache.streams.core.StreamsProvider;
import org.joda.time.DateTime;

import java.math.BigInteger;

/**
 * Created by sblackmon on 3/25/14.
 */
public class StreamsPigBuilder implements StreamBuilder {
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
