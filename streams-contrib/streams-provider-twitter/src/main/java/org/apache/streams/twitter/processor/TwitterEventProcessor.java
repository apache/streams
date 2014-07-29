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

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import org.apache.commons.lang3.StringUtils;
import org.apache.streams.core.StreamsDatum;
import org.apache.streams.core.StreamsProcessor;
import org.apache.streams.exceptions.ActivitySerializerException;
import org.apache.streams.jackson.StreamsJacksonMapper;
import org.apache.streams.pojo.json.Activity;
import org.apache.streams.twitter.pojo.Delete;
import org.apache.streams.twitter.pojo.Retweet;
import org.apache.streams.twitter.pojo.Tweet;
import org.apache.streams.twitter.provider.TwitterEventClassifier;
import org.apache.streams.twitter.serializer.*;
import org.apache.streams.util.ComponentUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;

/**
 * Created by sblackmon on 12/10/13.
 */
public class TwitterEventProcessor implements StreamsProcessor, Callable<List<StreamsDatum>> {

    private final static String STREAMS_ID = "TwitterEventProcessor";

    private final static Logger LOGGER = LoggerFactory.getLogger(TwitterEventProcessor.class);

    private ObjectMapper mapper = new StreamsTwitterMapper();

    private Class inClass;
    private Class outClass;
    private String item;

    private TwitterJsonActivitySerializer twitterJsonActivitySerializer;

    public TwitterEventProcessor(Class inClass, Class outClass, String item) {
        this.inClass = inClass;
        this.outClass = outClass;
        this.item = item;
    }

    public TwitterEventProcessor(Class inClass, Class outClass) {
        this(inClass, outClass, null);
    }

    public TwitterEventProcessor( Class outClass) {
        this(null, outClass, null);
    }

    public TwitterEventProcessor( Class outClass, String item) {
        this(null, outClass, item);
    }

    @Override
    public List<StreamsDatum> call() throws Exception {
        if(item != null) {
            ObjectNode objectNode = (ObjectNode) mapper.readTree(item);
            StreamsDatum rawDatum = new StreamsDatum(objectNode);
            return process(rawDatum);
        }
        return Lists.newArrayList();
    }

    public Object convert(ObjectNode event, Class inClass, Class outClass) throws ActivitySerializerException, JsonProcessingException {

        Object result = null;

        Preconditions.checkNotNull(event);
        Preconditions.checkNotNull(mapper);
        Preconditions.checkNotNull(twitterJsonActivitySerializer);

        if( outClass.equals( Activity.class )) {
                LOGGER.debug("ACTIVITY");
                result = twitterJsonActivitySerializer.deserialize(
                        mapper.writeValueAsString(event));
        } else if( outClass.equals( Tweet.class )) {
            if ( inClass.equals( Tweet.class )) {
                LOGGER.debug("TWEET");
                result = mapper.convertValue(event, Tweet.class);
            }
        } else if( outClass.equals( Retweet.class )) {
            if ( inClass.equals( Retweet.class )) {
                LOGGER.debug("RETWEET");
                result = mapper.convertValue(event, Retweet.class);
            }
        } else if( outClass.equals( Delete.class )) {
            if ( inClass.equals( Delete.class )) {
                LOGGER.debug("DELETE");
                result = mapper.convertValue(event, Delete.class);
            }
        } else if( outClass.equals( ObjectNode.class )) {
            LOGGER.debug("OBJECTNODE");
            result = mapper.convertValue(event, ObjectNode.class);
        }

            // no supported conversion were applied
        if( result != null )
            return result;

        LOGGER.debug("CONVERT FAILED");

        return null;

    }

    public boolean validate(Object document, Class klass) {

        // TODO
        return true;
    }

    public boolean isValidJSON(final String json) {
        boolean valid = false;
        try {
            final JsonParser parser = new ObjectMapper().getJsonFactory()
                    .createJsonParser(json);
            while (parser.nextToken() != null) {
            }
            valid = true;
        } catch (JsonParseException jpe) {
            LOGGER.warn("validate: {}", jpe);
        } catch (IOException ioe) {
            LOGGER.warn("validate: {}", ioe);
        }

        return valid;
    }

    @Override
    public List<StreamsDatum> process(StreamsDatum entry) {

        // first check for valid json
        ObjectNode node = (ObjectNode) entry.getDocument();

        LOGGER.debug("{} processing {}", STREAMS_ID, node.getClass());

        String json = null;
        try {
            json = mapper.writeValueAsString(node);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        if( StringUtils.isNotEmpty(json)) {

            // since data is coming from outside provider, we don't know what type the events are
            Class inClass = TwitterEventClassifier.detectClass(json);

            // if the target is string, just pass-through
            if (java.lang.String.class.equals(outClass))
                return Lists.newArrayList(new StreamsDatum(json));
            else {
                // convert to desired format
                Object out = null;
                try {
                    out = convert(node, inClass, outClass);
                } catch (ActivitySerializerException e) {
                    LOGGER.warn("Failed deserializing", e);
                    return Lists.newArrayList();
                } catch (JsonProcessingException e) {
                    LOGGER.warn("Failed parsing JSON", e);
                    return Lists.newArrayList();
                }

                if (out != null && validate(out, outClass))
                    return Lists.newArrayList(new StreamsDatum(out));
            }
        }

        return Lists.newArrayList();

    }

    @Override
    public void prepare(Object configurationObject) {
        mapper = new StreamsJacksonMapper();
        twitterJsonActivitySerializer = new TwitterJsonActivitySerializer();
    }

    @Override
    public void cleanUp() {

    }
};
