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

package org.apache.streams.twitter.processor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import org.apache.streams.core.StreamsDatum;
import org.apache.streams.core.StreamsProcessor;
import org.apache.streams.jackson.StreamsJacksonMapper;
import org.apache.streams.twitter.converter.StreamsTwitterMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * This class performs conversion of a twitter event to a specified outClass
 *
 * Deprecated: use TypeConverterProcessor and ActivityConverterProcessor instead
 */
@Deprecated
public class TwitterEventProcessor implements StreamsProcessor {

    private final static String STREAMS_ID = "TwitterEventProcessor";

    private final static Logger LOGGER = LoggerFactory.getLogger(TwitterEventProcessor.class);

    private ObjectMapper mapper = new StreamsTwitterMapper();

    private Class inClass;
    private Class outClass;

    public TwitterEventProcessor(Class inClass, Class outClass) {
        this.inClass = inClass;
        this.outClass = outClass;
    }

    public TwitterEventProcessor( Class outClass) {
        this(null, outClass);
    }

    @Override
    public String getId() {
        return STREAMS_ID;
    }

    @Override
    public List<StreamsDatum> process(StreamsDatum entry) {

        LOGGER.error("You are calling a deprecated / defunct class.  Modify your stream to use ActivityConverterProcessor.");

        LOGGER.debug("CONVERT FAILED");

        return Lists.newArrayList();

    }

    @Override
    public void prepare(Object configurationObject) {
        mapper = StreamsJacksonMapper.getInstance();
    }

    @Override
    public void cleanUp() {

    }
};
