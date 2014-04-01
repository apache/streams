package org.apache.streams.urls;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.google.common.collect.Lists;
import org.apache.streams.jackson.StreamsJacksonMapper;
import org.apache.streams.jackson.StreamsJacksonModule;
import org.apache.streams.urls.Link;
import org.apache.streams.urls.LinkUnwinder;
import org.apache.commons.lang.NotImplementedException;
import org.apache.streams.core.StreamsDatum;
import org.apache.streams.core.StreamsProcessor;
import org.apache.streams.pojo.json.Activity;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;

/**
 * References:
 * Some helpful references to help
 * Purpose              URL
 * -------------        ----------------------------------------------------------------
 * [Status Codes]       http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html
 * [Test Cases]         http://greenbytes.de/tech/tc/httpredirects/
 * [t.co behavior]      https://dev.twitter.com/docs/tco-redirection-behavior
 */

public class LinkUnwinderProcessor implements StreamsProcessor
{
    private final static String STREAMS_ID = "LinkUnwinderProcessor";

    private final static Logger LOGGER = LoggerFactory.getLogger(LinkUnwinderProcessor.class);

    private static ObjectMapper mapper = StreamsJacksonMapper.getInstance();

    @Override
    public List<StreamsDatum> process(StreamsDatum entry) {

        List<StreamsDatum> result = Lists.newArrayList();

        LOGGER.debug("{} processing {}", STREAMS_ID, entry.getDocument().getClass());

        Activity activity;

        // get list of shared urls
        if( entry.getDocument() instanceof Activity) {
            activity = (Activity) entry.getDocument();

            activity.setLinks(unwind(activity.getLinks()));

            entry.setDocument(activity);

            result.add(entry);

            return result;
        } else if( entry.getDocument() instanceof String ) {

            try {
                activity = mapper.readValue((String) entry.getDocument(), Activity.class);
            } catch (Exception e) {
                e.printStackTrace();
                LOGGER.warn(e.getMessage());
                return(Lists.newArrayList(entry));
            }

            activity.setLinks(unwind(activity.getLinks()));

            try {
                entry.setDocument(mapper.writeValueAsString(activity));
            } catch (Exception e) {
                e.printStackTrace();
                LOGGER.warn(e.getMessage());
                return(Lists.newArrayList());
            }

            result.add(entry);

            return result;

        }
        else {
            //return(Lists.newArrayList(entry));
            return( Lists.newArrayList());
        }
    }

    @Override
    public void prepare(Object o) {
    }

    @Override
    public void cleanUp() {

    }

    private List<String> unwind(List<String> inputLinks) {
        List<String> outputLinks = Lists.newArrayList();
        for( String link : inputLinks ) {
            try {
                LinkUnwinder unwinder = new LinkUnwinder(link);
                unwinder.run();
                outputLinks.add(unwinder.getFinalURL());
            } catch (Exception e) {
                //if unwindable drop
                LOGGER.debug("Failed to unwind link : {}", link);
                LOGGER.debug("Exception unwinding link : {}", e);
                e.printStackTrace();
            }
        }
        return outputLinks;
    }
}