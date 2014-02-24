package org.apache.streams.twitter.provider;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.Lists;
import org.apache.streams.core.StreamsDatum;
import org.apache.streams.core.StreamsProcessor;
import org.apache.streams.pojo.json.Activity;
import org.apache.streams.twitter.pojo.Delete;
import org.apache.streams.twitter.pojo.Retweet;
import org.apache.streams.twitter.pojo.Tweet;
import org.apache.streams.twitter.serializer.TwitterJsonDeleteActivitySerializer;
import org.apache.streams.twitter.serializer.TwitterJsonRetweetActivitySerializer;
import org.apache.streams.twitter.serializer.TwitterJsonTweetActivitySerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.Queue;
import java.util.Random;
import java.util.concurrent.BlockingQueue;

/**
 * Created by sblackmon on 12/10/13.
 */
public class TwitterEventProcessor implements StreamsProcessor, Runnable {

    private final static Logger LOGGER = LoggerFactory.getLogger(TwitterEventProcessor.class);

    private ObjectMapper mapper = new ObjectMapper();

    private BlockingQueue<String> inQueue;
    private Queue<StreamsDatum> outQueue;

    private Class inClass;
    private Class outClass;

    private TwitterJsonTweetActivitySerializer twitterJsonTweetActivitySerializer = new TwitterJsonTweetActivitySerializer();
    private TwitterJsonRetweetActivitySerializer twitterJsonRetweetActivitySerializer = new TwitterJsonRetweetActivitySerializer();
    private TwitterJsonDeleteActivitySerializer twitterJsonDeleteActivitySerializer = new TwitterJsonDeleteActivitySerializer();

    public final static String TERMINATE = new String("TERMINATE");

    public TwitterEventProcessor(BlockingQueue<String> inQueue, Queue<StreamsDatum> outQueue, Class inClass, Class outClass) {
        this.inQueue = inQueue;
        this.outQueue = outQueue;
        this.inClass = inClass;
        this.outClass = outClass;
    }

    public TwitterEventProcessor(BlockingQueue<String> inQueue, Queue<StreamsDatum> outQueue, Class outClass) {
        this.inQueue = inQueue;
        this.outQueue = outQueue;
        this.outClass = outClass;
    }

    @Override
    public void run() {

        while(true) {
            String item;
            try {
                item = inQueue.poll();
                if(item instanceof String && item.equals(TERMINATE)) {
                    LOGGER.info("Terminating!");
                    break;
                }

                ObjectNode objectNode = (ObjectNode) mapper.readTree(item);

                StreamsDatum rawDatum = new StreamsDatum(objectNode);

                for( StreamsDatum entry : process(rawDatum)) {
                    outQueue.offer(entry);
                }

            } catch (Exception e) {
                e.printStackTrace();

            }
        }
    }

    public Object convert(ObjectNode event, Class inClass, Class outClass) {

        LOGGER.debug(event.toString());

        Object result = null;

        if( outClass.equals( Activity.class )) {
            if( inClass.equals( Delete.class )) {
                LOGGER.debug("ACTIVITY DELETE");
                result = twitterJsonDeleteActivitySerializer.convert(event);
            } else if ( inClass.equals( Retweet.class )) {
                LOGGER.debug("ACTIVITY RETWEET");
                result = twitterJsonRetweetActivitySerializer.convert(event);
            } else if ( inClass.equals( Tweet.class )) {
                LOGGER.debug("ACTIVITY TWEET");
                result = twitterJsonTweetActivitySerializer.convert(event);
            } else {
                return null;
            }
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

        String json = node.asText();

        // since data is coming from outside provider, we don't know what type the events are
        Class inClass = TwitterEventClassifier.detectClass(json);

        // if the target is string, just pass-through
        if( java.lang.String.class.equals(outClass))
            return Lists.newArrayList(new StreamsDatum(json));
        else {
            // convert to desired format
            Object out = convert(node, inClass, outClass);

            if( out != null && validate(out, outClass))
                return Lists.newArrayList(new StreamsDatum(out));
        }

        return Lists.newArrayList();

    }

    @Override
    public void prepare(Object configurationObject) {

    }

    @Override
    public void cleanUp() {

    }
};
