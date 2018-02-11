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
import org.apache.streams.core.StreamsPersistWriter;
import org.apache.streams.util.GuidUtils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Preconditions;
import com.squareup.tape.QueueFile;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Writes data to a buffer stored on the file-system.
 */
public class FileBufferPersistWriter implements StreamsPersistWriter, Serializable {

  private static final String STREAMS_ID = "FileBufferPersistWriter";

  private static final Logger LOGGER = LoggerFactory.getLogger(FileBufferPersistWriter.class);

  private volatile Queue<StreamsDatum> persistQueue;

  private ObjectMapper mapper;

  private FileBufferConfiguration config;

  private QueueFile queueFile;

  public FileBufferPersistWriter() {
    this(new ComponentConfigurator<>(FileBufferConfiguration.class).detectConfiguration());
  }

  public FileBufferPersistWriter(FileBufferConfiguration config) {
    this.config = config;
  }

  @Override
  public String getId() {
    return STREAMS_ID;
  }

  @Override
  public void write(StreamsDatum entry) {

    String key = entry.getId() != null ? entry.getId() : GuidUtils.generateGuid("filewriter");

    Preconditions.checkArgument(StringUtils.isNotBlank(key));
    Preconditions.checkArgument(entry.getDocument() instanceof String);
    Preconditions.checkArgument(StringUtils.isNotBlank((String) entry.getDocument()));

    byte[] item = ((String)entry.getDocument()).getBytes();
    try {
      queueFile.add(item);
    } catch (IOException ex) {
      ex.printStackTrace();
    }
  }

  @Override
  public void prepare(Object configurationObject) {

    mapper = new ObjectMapper();

    File file = new File( config.getPath());

    try {
      queueFile = new QueueFile(file);
    } catch (IOException ex) {
      ex.printStackTrace();
    }

    Preconditions.checkArgument(file.exists());
    Preconditions.checkArgument(file.canWrite());

    Objects.requireNonNull(queueFile);

    this.persistQueue  = new ConcurrentLinkedQueue<>();

  }

  @Override
  public void cleanUp() {
    try {
      queueFile.close();
    } catch (IOException ex) {
      ex.printStackTrace();
    } finally {
      queueFile = null;
    }
  }
}
