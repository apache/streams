package org.apache.streams.twitter.processor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.Lists;
import org.apache.streams.core.StreamsDatum;
import org.apache.streams.core.StreamsProcessor;
import org.apache.streams.jackson.StreamsJacksonMapper;
import org.apache.streams.twitter.pojo.Retweet;
import org.apache.streams.twitter.pojo.Tweet;
import org.apache.streams.twitter.pojo.User;
import org.apache.streams.twitter.provider.TwitterEventClassifier;
import org.apache.streams.twitter.serializer.StreamsTwitterMapper;
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

    private ObjectMapper mapper = new StreamsTwitterMapper();

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
                result.add(new StreamsDatum(user, user.getIdStr()));
            }
            else if ( inClass.equals( Retweet.class )) {
                LOGGER.debug("RETWEET");
                user = mapper.readValue(item, User.class);
                result.add(new StreamsDatum(user, user.getIdStr()));
            }
            else if( inClass.equals( User.class)) {
                LOGGER.debug("USER");
                user = mapper.readValue(item, User.class);
                result.add(new StreamsDatum(user, user.getIdStr()));
            }
            else {
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
