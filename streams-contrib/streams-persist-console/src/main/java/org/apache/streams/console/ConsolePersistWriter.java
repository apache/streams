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
import org.apache.streams.core.StreamsPersistWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class ConsolePersistWriter implements StreamsPersistWriter {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConsolePersistWriter.class);

    protected volatile Queue<StreamsDatum> persistQueue;

    private ObjectMapper mapper = new ObjectMapper();

    public ConsolePersistWriter() {
        this.persistQueue = new ConcurrentLinkedQueue<StreamsDatum>();
    }

    public ConsolePersistWriter(Queue<StreamsDatum> persistQueue) {
        this.persistQueue = persistQueue;
    }

    public void prepare(Object o) {
        Preconditions.checkNotNull(persistQueue);
    }

    public void cleanUp() {

    }

    @Override
    public void write(StreamsDatum entry) {

        try {

            String text = mapper.writeValueAsString(entry);

            System.out.println(text);
//            LOGGER.info(text);

        } catch (JsonProcessingException e) {
            LOGGER.warn("save: {}", e);
        }

    }

}
