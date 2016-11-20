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

import org.apache.streams.core.StreamsDatum;
import org.apache.streams.core.StreamsProcessor;
import org.apache.streams.jackson.StreamsJacksonMapper;
import org.apache.streams.pojo.json.Activity;

import com.fasterxml.jackson.databind.module.SimpleModule;
import com.google.api.services.plus.model.Person;
import com.google.common.collect.Lists;
import com.google.gplus.serializer.util.GPlusActivityDeserializer;
import com.google.gplus.serializer.util.GPlusEventClassifier;
import com.google.gplus.serializer.util.GPlusPersonDeserializer;
import com.google.gplus.serializer.util.GooglePlusActivityUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Queue;

/**
 * GooglePlusTypeConverter is a StreamsProcessor that converts gplus activities to activitystreams activities.
 */
public class GooglePlusTypeConverter implements StreamsProcessor {

  public static final String STREAMS_ID = "GooglePlusTypeConverter";

  private static final Logger LOGGER = LoggerFactory.getLogger(GooglePlusTypeConverter.class);
  private StreamsJacksonMapper mapper;
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
  public String getId() {
    return STREAMS_ID;
  }

  @Override
  public List<StreamsDatum> process(StreamsDatum entry) {
    StreamsDatum result = null;

    try {
      Object item = entry.getDocument();

      LOGGER.debug("{} processing {}", STREAMS_ID, item.getClass());
      Activity activity = null;

      if (item instanceof String) {
        item = deserializeItem(item);
      }

      if (item instanceof Person) {
        activity = new Activity();
        googlePlusActivityUtil.updateActivity((Person)item, activity);
      } else if (item instanceof com.google.api.services.plus.model.Activity) {
        activity = new Activity();
        googlePlusActivityUtil.updateActivity((com.google.api.services.plus.model.Activity)item, activity);
      }

      if (activity != null) {
        result = new StreamsDatum(activity);
        count++;
      }
    } catch (Exception ex) {
      ex.printStackTrace();
      LOGGER.error("Exception while converting Person to Activity: {}", ex.getMessage());
    }

    if ( result != null ) {
      return Lists.newArrayList(result);
    } else {
      return Lists.newArrayList();
    }
  }

  private Object deserializeItem(Object item) {
    try {
      Class klass = GPlusEventClassifier.detectClass((String) item);

      if (klass.equals(Person.class)) {
        item = mapper.readValue((String) item, Person.class);
      } else if (klass.equals(com.google.api.services.plus.model.Activity.class)) {
        item = mapper.readValue((String) item, com.google.api.services.plus.model.Activity.class);
      }
    } catch (Exception ex) {
      LOGGER.error("Exception while trying to deserializeItem: {}", ex);
    }

    return item;
  }

  @Override
  public void prepare(Object configurationObject) {
    googlePlusActivityUtil = new GooglePlusActivityUtil();
    mapper = StreamsJacksonMapper.getInstance();

    SimpleModule simpleModule = new SimpleModule();
    simpleModule.addDeserializer(Person.class, new GPlusPersonDeserializer());
    mapper.registerModule(simpleModule);

    simpleModule = new SimpleModule();
    simpleModule.addDeserializer(com.google.api.services.plus.model.Activity.class, new GPlusActivityDeserializer());
    mapper.registerModule(simpleModule);
  }

  @Override
  public void cleanUp() {
    //No-op
  }
}
