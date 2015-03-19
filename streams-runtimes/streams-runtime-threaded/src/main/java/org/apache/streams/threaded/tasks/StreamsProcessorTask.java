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
package org.apache.streams.threaded.tasks;

import org.apache.streams.threaded.controller.ThreadingController;
import org.apache.streams.core.StreamsDatum;
import org.apache.streams.core.StreamsProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Map;

/**
 * Processor task that is multi-threaded
 */
public class StreamsProcessorTask extends BaseStreamsTask {

    private final static Logger LOGGER = LoggerFactory.getLogger(StreamsProcessorTask.class);

    protected final StreamsProcessor processor;

    public StreamsProcessorTask(ThreadingController threadingController, String id, Map<String, Object> config, StreamsProcessor processor) {
        super(threadingController, id, config, processor);
        this.processor = processor;
    }

    protected Collection<StreamsDatum> processInternal(StreamsDatum datum) {
        return this.processor.process(datum);
    }
}