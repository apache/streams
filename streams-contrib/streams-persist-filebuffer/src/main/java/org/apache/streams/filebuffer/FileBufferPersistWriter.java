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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.squareup.tape.QueueFile;
import org.apache.streams.config.StreamsConfigurator;
import org.apache.streams.core.StreamsDatum;
import org.apache.streams.core.StreamsPersistWriter;
import org.apache.streams.filebuffer.FileBufferConfiguration;
import org.apache.streams.util.GuidUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Writes data to a buffer stored on the file-system.
 */
public class FileBufferPersistWriter implements StreamsPersistWriter, Serializable {

    private static final Logger LOGGER = LoggerFactory.getLogger(FileBufferPersistWriter.class);

    protected volatile Queue<StreamsDatum> persistQueue;

    private ObjectMapper mapper;

    private FileBufferConfiguration config;

    private QueueFile queueFile;

    public FileBufferPersistWriter() {
       this(FileBufferConfigurator.detectConfiguration(StreamsConfigurator.config.getConfig("filebuffer")));
    }

    public FileBufferPersistWriter(FileBufferConfiguration config) {
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
