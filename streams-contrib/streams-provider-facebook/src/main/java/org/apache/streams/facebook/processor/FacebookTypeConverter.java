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

package org.apache.streams.facebook.processor;

import org.apache.streams.core.StreamsDatum;
import org.apache.streams.core.StreamsProcessor;
import org.apache.streams.exceptions.ActivitySerializerException;
import org.apache.streams.facebook.Page;
import org.apache.streams.facebook.Post;
import org.apache.streams.facebook.api.FacebookPageActivitySerializer;
import org.apache.streams.facebook.api.FacebookPostActivitySerializer;
import org.apache.streams.facebook.provider.FacebookEventClassifier;
import org.apache.streams.jackson.StreamsJacksonMapper;
import org.apache.streams.pojo.json.Activity;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.Queue;

/**
 * FacebookTypeConverter converts facebook data to activity streams types.
 */
public class FacebookTypeConverter implements StreamsProcessor {

  public static final String STREAMS_ID = "FacebookTypeConverter";

  private static final Logger LOGGER = LoggerFactory.getLogger(FacebookTypeConverter.class);

  private ObjectMapper mapper;

  private Queue<StreamsDatum> inQueue;
  private Queue<StreamsDatum> outQueue;

  private Class inClass;
  private Class outClass;

  private FacebookPostActivitySerializer facebookPostActivitySerializer;
  private FacebookPageActivitySerializer facebookPageActivitySerializer;

  private int count = 0;

  public static final String TERMINATE = "TERMINATE";

  public FacebookTypeConverter(Class inClass, Class outClass) {
    this.inClass = inClass;
    this.outClass = outClass;
  }

  public Queue<StreamsDatum> getProcessorOutputQueue() {
    return outQueue;
  }

  public void setProcessorInputQueue(Queue<StreamsDatum> inputQueue) {
    inQueue = inputQueue;
  }

  /**
   * convert.
   * @param event event
   * @param inClass inClass
   * @param outClass outClass
   * @return Object
   * @throws ActivitySerializerException ActivitySerializerException
   * @throws JsonProcessingException JsonProcessingException
   */
  public Object convert(ObjectNode event, Class inClass, Class outClass) throws ActivitySerializerException, JsonProcessingException {

    Object result = null;

    if ( outClass.equals( Activity.class )) {
      LOGGER.debug("ACTIVITY");
      if (inClass.equals(Post.class)) {
        LOGGER.debug("POST");
        result = facebookPostActivitySerializer.deserialize(mapper.convertValue(event, Post.class));
      } else if (inClass.equals(Page.class)) {
        LOGGER.debug("PAGE");
        result = facebookPageActivitySerializer.deserialize(mapper.convertValue(event, Page.class));
      }
    } else if ( outClass.equals( Post.class )) {
      LOGGER.debug("POST");
      result = mapper.convertValue(event, Post.class);
    } else if ( outClass.equals(Page.class)) {
      LOGGER.debug("PAGE");
      result = mapper.convertValue(event, Page.class);
    } else if ( outClass.equals( ObjectNode.class )) {
      LOGGER.debug("OBJECTNODE");
      result = mapper.convertValue(event, ObjectNode.class);
    }

    // no supported conversion were applied
    if ( result != null ) {
      count ++;
      return result;
    }

    LOGGER.debug("CONVERT FAILED");

    return null;
  }

  // TODO: use standard validation
  public boolean validate(Object document, Class klass) {
    return true;
  }

  // TODO: replace with standard validation
  public boolean isValidJSON(final String json) {
    boolean valid = false;
    try {
      final JsonParser parser = new ObjectMapper().getJsonFactory()
          .createJsonParser(json);
      while (parser.nextToken() != null) {
      }
      valid = true;
    } catch (IOException ioe) {
      LOGGER.warn("validate: {}", ioe);
    }

    return valid;
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
      ObjectNode node;

      LOGGER.debug("{} processing {}", STREAMS_ID, item.getClass());

      if ( item instanceof String ) {

        // if the target is string, just pass-through
        if ( String.class.equals(outClass)) {
          result = entry;
        } else {
          // first check for valid json
          node = (ObjectNode)mapper.readTree((String)item);

          // since data is coming from outside provider, we don't know what type the events are
          // for now we'll assume post
          Class inClass = FacebookEventClassifier.detectClass((String) item);

          Object out = convert(node, inClass, outClass);

          if ( out != null && validate(out, outClass)) {
            result = new StreamsDatum(out);
          }
        }

      } else if ( item instanceof ObjectNode) {

        // first check for valid json
        node = (ObjectNode)mapper.valueToTree(item);

        Class inClass = FacebookEventClassifier.detectClass(mapper.writeValueAsString(item));

        Object out = convert(node, inClass, outClass);

        if ( out != null && validate(out, outClass)) {
          result = new StreamsDatum(out);
        }
      } else if (item instanceof Post || item instanceof Page) {
        Object out = convert(mapper.convertValue(item, ObjectNode.class), inClass, outClass);

        if ( out != null && validate(out, outClass)) {
          result = new StreamsDatum(out);
        }
      }
    } catch (Exception ex) {
      LOGGER.error("Exception switching types : {}", ex);
      if (ex instanceof InterruptedException) {
        Thread.currentThread().interrupt();
      }
    }

    if ( result != null ) {
      return Lists.newArrayList(result);
    } else {
      return Lists.newArrayList();
    }
  }

  @Override
  public void prepare(Object configurationObject) {
    mapper = StreamsJacksonMapper.getInstance();

    facebookPageActivitySerializer = new FacebookPageActivitySerializer();
    facebookPostActivitySerializer = new FacebookPostActivitySerializer();
  }

  @Override
  public void cleanUp() {}
}
