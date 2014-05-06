package org.apache.streams.urls;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import org.apache.streams.jackson.StreamsJacksonMapper;
import org.apache.streams.core.StreamsDatum;
import org.apache.streams.core.StreamsProcessor;
import org.apache.streams.pojo.json.Activity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

public class LinkResolverProcessor implements StreamsProcessor
{
    private final static String STREAMS_ID = "LinkResolverProcessor";

    private final static Logger LOGGER = LoggerFactory.getLogger(LinkResolverProcessor.class);

    private static ObjectMapper mapper = StreamsJacksonMapper.getInstance();

    @Override
    public List<StreamsDatum> process(StreamsDatum entry) {

        List<StreamsDatum> result = Lists.newArrayList();

        LOGGER.debug("{} processing {}", STREAMS_ID, entry.getDocument().getClass());

        Activity activity;

        // get list of shared urls
        if( entry.getDocument() instanceof Activity) {
            activity = (Activity) entry.getDocument();

        } else if( entry.getDocument() instanceof String ) {

            try {
                activity = mapper.readValue((String) entry.getDocument(), Activity.class);
            } catch (Exception e) {
                e.printStackTrace();
                LOGGER.warn(e.getMessage());
                return(Lists.newArrayList(entry));
            }

        }
        else {
            //return(Lists.newArrayList(entry));
            return( Lists.newArrayList());
        }

        for( int i = 0; i < activity.getLinks().size(); i++ )
        {
            Object linkObject = activity.getLinks().get(i);
            String linkUrl;
            try {
                if( linkObject instanceof String )
                    linkUrl = (String) linkObject;
                else if( linkObject instanceof LinkDetails )
                    linkUrl = (String)((LinkDetails)linkObject).getAdditionalProperties().get("originalURL");
                else {
                    LOGGER.warn("can't locate url in doc");
                    return result;
                }
                LinkDetails details = resolve(linkUrl);
                if( details != null ) {
                    activity.getLinks().set(i, details);
                } else {
                    activity.getLinks().remove(i);
                }
            } catch (Exception e) {
                e.printStackTrace();
                LOGGER.warn(e.getMessage());
                activity.getLinks().remove(i);
            }

        }

        try {
            entry.setDocument(activity);
        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.warn(e.getMessage());
            return(Lists.newArrayList());
        }

        result.add(entry);

        return result;
    }

    @Override
    public void prepare(Object o) {
    }

    @Override
    public void cleanUp() {

    }

    private LinkDetails resolve(String inputLink) {
        LinkDetails result;
        try {
            LinkResolver resolver = new LinkResolver(inputLink);
            resolver.run();
            result = resolver.getLinkDetails();
            return result;
        } catch (Exception e) {
            //if resolvable drop
            LOGGER.debug("Failed to resolve link : {}", inputLink);
            LOGGER.debug("Exception resolving link : {}", e);
            e.printStackTrace();
            return null;
        }
    }
}