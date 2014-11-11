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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.squareup.tape.QueueFile;
import com.typesafe.config.Config;
import org.apache.streams.config.StreamsConfigurator;
import org.apache.streams.core.StreamsDatum;
import org.apache.streams.core.StreamsPersistWriter;
import org.apache.streams.util.GuidUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Properties;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class FilePersistWriter implements StreamsPersistWriter, Serializable {

    private static final Logger LOGGER = LoggerFactory.getLogger(FilePersistWriter.class);

    protected volatile Queue<StreamsDatum> persistQueue;

    private ObjectMapper mapper;

    private FileConfiguration config;

    private QueueFile queueFile;

    public FilePersistWriter() {
       this(FileConfigurator.detectConfiguration(StreamsConfigurator.config.getConfig("file")));
    }

    public FilePersistWriter(FileConfiguration config) {
        this.config = config;
    }

    @Override
    public void write(StreamsDatum entry) {

        String key = entry.getId() != null ? entry.getId() : GuidUtils.generateGuid("filewriter");

        Preconditions.checkArgument(Strings.isNullOrEmpty(key) == false);
        Preconditions.checkArgument(entry.getDocument() instanceof String);
        Preconditions.checkArgument(Strings.isNullOrEmpty((String)entry.getDocument()) == false);

        byte[] item = ((String)entry.getDocument()).getBytes();
        try {
            queueFile.add(item);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void prepare(Object configurationObject) {

        mapper = new ObjectMapper();

        File file = new File( config.getPath());

        try {
            queueFile = new QueueFile(file);
        } catch (IOException e) {
            e.printStackTrace();
        }

        Preconditions.checkArgument(file.exists());
        Preconditions.checkArgument(file.canWrite());

        Preconditions.checkNotNull(queueFile);

        this.persistQueue  = new ConcurrentLinkedQueue<StreamsDatum>();

    }

    @Override
    public void cleanUp() {
        try {
            queueFile.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            queueFile = null;
        }
    }
}
