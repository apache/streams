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

package org.apache.streams.console;

import org.apache.streams.core.StreamsDatum;
import org.apache.streams.core.StreamsPersistWriter;
import org.apache.streams.jackson.StreamsJacksonMapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.PrintStream;
import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * ConsolePersistWriter writes documents to stdout.
 */
public class ConsolePersistWriter implements StreamsPersistWriter {

  private static final String STREAMS_ID = "ConsolePersistWriter";

  private static final Logger LOGGER = LoggerFactory.getLogger(ConsolePersistWriter.class);

  private PrintStream printStream = System.out;

  protected volatile Queue<StreamsDatum> persistQueue;

  private ObjectMapper mapper = StreamsJacksonMapper.getInstance();

  public ConsolePersistWriter() {
    this.persistQueue = new ConcurrentLinkedQueue<>();
  }

  public ConsolePersistWriter(PrintStream printStream) {
    this();
    this.printStream = printStream;
  }

  @Override
  public String getId() {
    return STREAMS_ID;
  }

  public void prepare(Object configuration) {
    Objects.requireNonNull(persistQueue);
  }

  public void cleanUp() {

  }

  @Override
  public void write(StreamsDatum entry) {

    try {

      String text = mapper.writeValueAsString(entry);

      printStream.println(text);

    } catch (JsonProcessingException ex) {
      LOGGER.warn("save: {}", ex);
    }

  }

}
