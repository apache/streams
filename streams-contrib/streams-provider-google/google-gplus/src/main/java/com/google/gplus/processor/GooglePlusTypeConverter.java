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

package com.google.gplus.processor;

import com.google.api.services.plus.model.Person;
import com.google.common.collect.Lists;
import com.google.gplus.serializer.util.GooglePlusActivityUtil;
import org.apache.streams.core.StreamsDatum;
import org.apache.streams.core.StreamsProcessor;
import org.apache.streams.pojo.json.Activity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Queue;

public class GooglePlusTypeConverter implements StreamsProcessor {
    public final static String STREAMS_ID = "GooglePlusTypeConverter";

    private final static Logger LOGGER = LoggerFactory.getLogger(GooglePlusTypeConverter.class);
    private Queue<Person> inQueue;
    private Queue<StreamsDatum> outQueue;
    private GooglePlusActivityUtil googlePlusActivityUtil;
    private int count = 0;

    public GooglePlusTypeConverter() {}

    public Queue<StreamsDatum> getProcessorOutputQueue() {
        return outQueue;
    }

    public void setProcessorInputQueue(Queue<Person> inputQueue) {
        inQueue = inputQueue;
    }

    @Override
    public List<StreamsDatum> process(StreamsDatum entry) {
        StreamsDatum result = null;

        try {
            Object item = entry.getDocument();

            LOGGER.debug("{} processing {}", STREAMS_ID, item.getClass());
            Activity activity = null;

            if(item instanceof Person) {
                activity = new Activity();
                googlePlusActivityUtil.updateActivity((Person)item, activity);
            } else if(item instanceof com.google.api.services.plus.model.Activity) {
                activity = new Activity();
                googlePlusActivityUtil.updateActivity((com.google.api.services.plus.model.Activity)item, activity);
            }

            if(activity != null) {
                result = new StreamsDatum(activity);
                count++;
            }
        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.error("Exception while converting Person to Activity: {}", e.getMessage());
        }

        if( result != null )
            return Lists.newArrayList(result);
        else
            return Lists.newArrayList();
    }

    @Override
    public void prepare(Object configurationObject) {
        googlePlusActivityUtil = new GooglePlusActivityUtil();
    }

    @Override
    public void cleanUp() {
        //No-op
    }
}
