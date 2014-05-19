package org.apache.streams.twitter.processor;

/*
 * #%L
 * streams-provider-twitter
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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.Lists;
import org.apache.streams.core.StreamsDatum;
import org.apache.streams.core.StreamsProcessor;
import org.apache.streams.twitter.pojo.Retweet;
import org.apache.streams.twitter.pojo.Tweet;
import org.apache.streams.twitter.pojo.User;
import org.apache.streams.twitter.provider.TwitterEventClassifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Queue;
import java.util.Random;

/**
 * Created by sblackmon on 12/10/13.
 */
public class TwitterProfileProcessor implements StreamsProcessor, Runnable {

    private final static Logger LOGGER = LoggerFactory.getLogger(TwitterProfileProcessor.class);

    private ObjectMapper mapper = new ObjectMapper();

    private Queue<StreamsDatum> inQueue;
    private Queue<StreamsDatum> outQueue;

    public final static String TERMINATE = new String("TERMINATE");

    @Override
    public void run() {

        while(true) {
            StreamsDatum item;
                try {
                    item = inQueue.poll();
                    if(item.getDocument() instanceof String && item.equals(TERMINATE)) {
                        LOGGER.info("Terminating!");
                        break;
                    }

                    Thread.sleep(new Random().nextInt(100));

                    for( StreamsDatum entry : process(item)) {
                        outQueue.offer(entry);
                    }


            } catch (Exception e) {
                e.printStackTrace();

            }
        }
    }

    @Override
    public List<StreamsDatum> process(StreamsDatum entry) {

        List<StreamsDatum> result = Lists.newArrayList();
        String item;
        try {
            // first check for valid json
            // since data is coming from outside provider, we don't know what type the events are
            if( entry.getDocument() instanceof String) {
                item = (String) entry.getDocument();
            } else {
                item = mapper.writeValueAsString((ObjectNode)entry.getDocument());
            }

            Class inClass = TwitterEventClassifier.detectClass(item);

            User user;

            if ( inClass.equals( Tweet.class )) {
                LOGGER.debug("TWEET");
                Tweet tweet = mapper.readValue(item, Tweet.class);
                user = tweet.getUser();
                result.add(new StreamsDatum(user));
            }
            else if ( inClass.equals( Retweet.class )) {
                LOGGER.debug("RETWEET");
                Retweet retweet = mapper.readValue(item, Retweet.class);
                user = retweet.getRetweetedStatus().getUser();
                result.add(new StreamsDatum(user));
            } else {
                return Lists.newArrayList();
            }

            return result;
        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.warn("Error processing " + entry.toString());
            return Lists.newArrayList();
        }
    }

    @Override
    public void prepare(Object o) {

    }

    @Override
    public void cleanUp() {

    }
};
