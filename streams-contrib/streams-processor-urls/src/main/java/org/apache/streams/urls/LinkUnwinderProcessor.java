package org.apache.streams.urls;

import com.google.common.collect.Lists;
import org.apache.streams.urls.Link;
import org.apache.streams.urls.LinkUnwinder;
import org.apache.commons.lang.NotImplementedException;
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

public class LinkUnwinderProcessor implements StreamsProcessor
{
    private final static String STREAMS_ID = "LinkUnwinderProcessor";

    private final static Logger LOGGER = LoggerFactory.getLogger(LinkUnwinderProcessor.class);



    @Override
    public List<StreamsDatum> process(StreamsDatum entry) {

        List<StreamsDatum> result = Lists.newArrayList();

        LOGGER.debug("{} processing {}", STREAMS_ID, entry.getDocument().getClass());

        // get list of shared urls
        if( entry.getDocument() instanceof Activity) {
            Activity activity = (Activity) entry.getDocument();
            List<String> inputLinks = activity.getLinks();
            List<String> outputLinks = Lists.newArrayList();
            for( String link : inputLinks ) {
                try {
                    LinkUnwinder unwinder = new LinkUnwinder((String)link);
                    unwinder.run();
                    if( !unwinder.isFailure()) {
                        outputLinks.add(unwinder.getFinalURL());
                    }
                } catch (Exception e) {
                    //if unwindable drop
                    LOGGER.debug("Failed to unwind link : {}", link);
                    LOGGER.debug("Excpetion unwind link : {}", e);
                }
            }
            activity.setLinks(outputLinks);
            entry.setDocument(activity);
            result.add(entry);

            return result;
        }
        else throw new NotImplementedException();
    }

    @Override
    public void prepare(Object o) {
    }

    @Override
    public void cleanUp() {

    }

}