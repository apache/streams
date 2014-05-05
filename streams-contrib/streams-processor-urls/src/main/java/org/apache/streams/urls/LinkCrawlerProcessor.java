package org.apache.streams.urls;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsonorg.JsonOrgModule;
import com.google.common.collect.Lists;
import org.apache.commons.lang.NotImplementedException;
import org.apache.streams.core.StreamsDatum;
import org.apache.streams.core.StreamsProcessor;
import org.apache.streams.jackson.StreamsJacksonMapper;
import org.apache.streams.pojo.json.Activity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * References:
 * Some helpful references to help
 * Purpose              URL
 * -------------        ----------------------------------------------------------------
 * [Status Codes]       http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html
 * [Test Cases]         http://greenbytes.de/tech/tc/httpredirects/
 * [t.co behavior]      https://dev.twitter.com/docs/tco-redirection-behavior
 */

public class LinkCrawlerProcessor implements StreamsProcessor
{
    public final static String STREAMS_ID = "LinkCrawlerProcessor";

    private final static Logger LOGGER = LoggerFactory.getLogger(LinkCrawlerProcessor.class);

    private ObjectMapper mapper;

    @Override
    public List<StreamsDatum> process(StreamsDatum entry) {

        List<StreamsDatum> result = Lists.newArrayList();

        LOGGER.debug("{} processing {}", STREAMS_ID, entry.getDocument().getClass());

        Activity activity;

        System.out.println( STREAMS_ID + " processing " + entry.getDocument().getClass());
        // get list of shared urls
        if( entry.getDocument() instanceof Activity) {

            activity = (Activity) entry.getDocument();

        }
        else if(entry.getDocument() instanceof String) {

            try {
                activity = mapper.readValue((String) entry.getDocument(), Activity.class);
            } catch (Exception e) {
                e.printStackTrace();
                LOGGER.warn(e.getMessage());
                return(Lists.newArrayList(entry));
            }

        }
        else throw new NotImplementedException();

        for( int i = 0; i < activity.getLinks().size(); i++ )
        {
            Object linkObject = activity.getLinks().get(i);
            String linkUrl;
            try {
                if( linkObject instanceof String )
                    linkUrl = (String) linkObject;
                else if( linkObject instanceof LinkDetails )
                    linkUrl = (String)((LinkDetails)linkObject).getOriginalURL();
                else {
                    LOGGER.warn("can't locate url in doc");
                    return result;
                }
                LinkDetails details = crawlLink(linkUrl, entry);
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

        return result;
    }

    private LinkDetails crawlLink(String link, StreamsDatum input) {

        LinkCrawler crawler = new LinkCrawler((String)link);
        crawler.run();
        return crawler.getLinkDetails();

    }

    @Override
    public void prepare(Object o) {
        this.mapper = StreamsJacksonMapper.getInstance();
        this.mapper.registerModule(new JsonOrgModule());
    }

    @Override
    public void cleanUp() {

    }

}