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

package org.apache.streams.file;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Queues;
import com.squareup.tape.QueueFile;
import org.apache.streams.config.StreamsConfigurator;
import org.apache.streams.core.StreamsDatum;
import org.apache.streams.core.StreamsPersistReader;
import org.apache.streams.core.StreamsResultSet;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Queue;
import java.util.Random;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class FilePersistReader implements StreamsPersistReader, Serializable {

    private static final Logger LOGGER = LoggerFactory.getLogger(FilePersistReader.class);

    protected volatile Queue<StreamsDatum> persistQueue;

    private ObjectMapper mapper = new ObjectMapper();

    private FileConfiguration config;

    private QueueFile queueFile;

    private boolean isStarted = false;
    private boolean isStopped = false;

    private ExecutorService executor = Executors.newSingleThreadExecutor();

    public FilePersistReader() {
        this(FileConfigurator.detectConfiguration(StreamsConfigurator.config.getConfig("file")));
    }

    public FilePersistReader(FileConfiguration config) {
        this.config = config;
    }

    @Override
    public StreamsResultSet readAll() {
        return readCurrent();
    }

    @Override
    public void startStream() {
        isStarted = true;
    }

    @Override
    public StreamsResultSet readCurrent() {

        while (!queueFile.isEmpty()) {
            try {
                byte[] bytes = queueFile.peek();
                ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
                BufferedReader buf = new BufferedReader(new InputStreamReader(bais));
                String s = buf.readLine();
                System.out.println(s);
                write(new StreamsDatum(s));
                queueFile.remove();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        StreamsResultSet current;
        current = new StreamsResultSet(Queues.newConcurrentLinkedQueue(persistQueue));
        persistQueue.clear();

        return current;
    }

    private void write( StreamsDatum entry ) {
        persistQueue.offer(entry);
    }

    @Override
    public StreamsResultSet readNew(BigInteger bigInteger) {
        return null;
    }

    @Override
    public StreamsResultSet readRange(DateTime dateTime, DateTime dateTime2) {
        return null;
    }

    @Override
    public boolean isRunning() {
        return isStarted && !isStopped;
    }

    @Override
    public void prepare(Object configurationObject) {

        try {
            Thread.sleep(1000);
        } catch (InterruptedException ie) {
            //Handle exception
        }

        try {
            queueFile = new QueueFile(new File(config.getFile()));
        } catch (IOException e) {
            e.printStackTrace();
        }

        Preconditions.checkNotNull(queueFile);

        this.persistQueue = new ConcurrentLinkedQueue<>();

    }

        @Override
    public void cleanUp() {
        try {
            queueFile.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            queueFile = null;
            isStopped = true;
        }
    }
}
