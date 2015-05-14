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

package org.apache.streams.sprinklr.processor;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.google.common.collect.Lists;
import org.apache.streams.core.StreamsDatum;
import org.apache.streams.core.StreamsProcessor;
import org.apache.streams.sprinklr.util.SprinklrDataToActivityConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.streams.pojo.json.Activity;

import java.util.List;

/**
 * Sprinklr type converter for streams
 */
public class SprinklrTypeConverter implements StreamsProcessor {

    public final static String STREAMS_ID = "SprinklrTypeConverter";

    private final static Logger LOGGER = LoggerFactory.getLogger(SprinklrTypeConverter.class);

    private SprinklrDataToActivityConverter activityConverter;

    private int count = 0;

    @Override
    public List<StreamsDatum> process(StreamsDatum entry) {
        List<StreamsDatum> streams = Lists.newArrayList();

        try {
            Object item = entry.document;

            // check if ArrayNode and iterate over the JsonNodes
            if (item instanceof ArrayNode) {
                while (((ArrayNode) item).elements().hasNext()) {
                    JsonNode dataItem = ((ArrayNode) item).elements().next();
                    LOGGER.debug("{} processing {}", STREAMS_ID, dataItem.getClass());
                    streams.add(new StreamsDatum(activityConverter.convert(dataItem)));
                    count++;
                }
            }

            // maybe item is a JsonNode
            else if (item instanceof JsonNode) {
                LOGGER.debug("{} processing {}", STREAMS_ID, item.getClass());
                streams.add(new StreamsDatum(activityConverter.convert((JsonNode) item)));
                count++;
            }

            // maybe it's something we don't even know about yet
            else {
                LOGGER.info("{} processing unknown item class={}", STREAMS_ID, item.getClass());
            }

        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.error("Exception while converting SprinklrData to Activity: {}", e.getMessage());
        }
        return streams;
    }

    @Override
    public void prepare(Object configurationObject) {
        activityConverter = new SprinklrDataToActivityConverter();
    }

    @Override
    public void cleanUp() {

    }
}
