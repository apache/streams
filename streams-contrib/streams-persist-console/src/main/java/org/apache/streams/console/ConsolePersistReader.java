package org.apache.streams.console;

/*
 * #%L
 * streams-persist-console
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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Preconditions;
import org.apache.streams.core.StreamsDatum;
import org.apache.streams.core.StreamsPersistReader;
import org.apache.streams.core.StreamsPersistWriter;
import org.apache.streams.core.StreamsResultSet;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.math.BigInteger;
import java.util.Queue;
import java.util.Scanner;
import java.util.concurrent.ConcurrentLinkedQueue;

public class ConsolePersistReader implements StreamsPersistReader {

    private final static String STREAMS_ID = "ConsolePersistReader";

    private static final Logger LOGGER = LoggerFactory.getLogger(ConsolePersistReader.class);

    protected volatile Queue<StreamsDatum> persistQueue;

    private ObjectMapper mapper = new ObjectMapper();

    public ConsolePersistReader() {
        this.persistQueue = new ConcurrentLinkedQueue<StreamsDatum>();
    }

    public ConsolePersistReader(Queue<StreamsDatum> persistQueue) {
        this.persistQueue = persistQueue;
    }

    public void prepare(Object o) {

    }

    public void cleanUp() {

    }

    @Override
    public void startStream() {
        // no op
    }

    @Override
    public StreamsResultSet readAll() {
        return readCurrent();
    }

    @Override
    public StreamsResultSet readCurrent() {

        LOGGER.info("{} readCurrent", STREAMS_ID);

        Scanner sc = new Scanner(System.in);

        while( sc.hasNextLine() ) {

            persistQueue.offer(new StreamsDatum(sc.nextLine()));

        }

        LOGGER.info("Providing {} docs", persistQueue.size());

        StreamsResultSet result =  new StreamsResultSet(persistQueue);

        LOGGER.info("{} Exiting", STREAMS_ID);

        return result;

    }

    @Override
    public StreamsResultSet readNew(BigInteger sequence) {
        return readCurrent();
    }

    @Override
    public StreamsResultSet readRange(DateTime start, DateTime end) {
        return readCurrent();
    }
}
