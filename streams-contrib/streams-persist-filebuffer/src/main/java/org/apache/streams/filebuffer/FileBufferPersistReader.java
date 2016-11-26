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

package org.apache.streams.filebuffer;

import org.apache.streams.config.ComponentConfigurator;
import org.apache.streams.config.StreamsConfigurator;
import org.apache.streams.core.StreamsDatum;
import org.apache.streams.core.StreamsPersistReader;
import org.apache.streams.core.StreamsResultSet;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Preconditions;
import com.squareup.tape.QueueFile;
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
import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Reads data from a buffer stored on the file-system.
 */
public class FileBufferPersistReader implements StreamsPersistReader, Serializable {

  private static final String STREAMS_ID = "FileBufferPersistReader";

  private static final Logger LOGGER = LoggerFactory.getLogger(FileBufferPersistReader.class);

  private volatile Queue<StreamsDatum> persistQueue;

  private ObjectMapper mapper;

  private FileBufferConfiguration config;

  private QueueFile queueFile;

  private boolean isStarted = false;
  private boolean isStopped = false;

  private ExecutorService executor = Executors.newSingleThreadExecutor();

  public FileBufferPersistReader() {
    this(new ComponentConfigurator<>(FileBufferConfiguration.class)
        .detectConfiguration(StreamsConfigurator.getConfig().getConfig("filebuffer")));
  }

  public FileBufferPersistReader(FileBufferConfiguration config) {
    this.config = config;
  }

  @Override
  public String getId() {
    return STREAMS_ID;
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
        String line = buf.readLine();
        LOGGER.debug(line);
        write(new StreamsDatum(line));
        queueFile.remove();
      } catch (IOException ex) {
        ex.printStackTrace();
      }
    }

    StreamsResultSet current;
    current = new StreamsResultSet(new ConcurrentLinkedQueue<>(persistQueue));
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

    mapper = new ObjectMapper();

    File file = new File( config.getPath());

    if ( !file.exists() ) {
      try {
        file.createNewFile();
      } catch (IOException ex) {
        LOGGER.error(ex.getMessage());
      }
    }

    Preconditions.checkArgument(file.exists());
    Preconditions.checkArgument(file.canRead());

    try {
      queueFile = new QueueFile(file);
    } catch (IOException ex) {
      LOGGER.error(ex.getMessage());
    }

    Objects.requireNonNull(queueFile);

    this.persistQueue = new ConcurrentLinkedQueue<>();

  }

  @Override
  public void cleanUp() {
    try {
      queueFile.close();
    } catch (IOException ex) {
      ex.printStackTrace();
    } finally {
      queueFile = null;
      isStopped = true;
    }
  }
}
