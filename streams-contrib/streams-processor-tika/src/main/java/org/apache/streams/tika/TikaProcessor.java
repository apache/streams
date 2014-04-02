package org.apache.streams.tika;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsonorg.JsonOrgModule;
import com.google.common.collect.Lists;
import org.apache.commons.lang.NotImplementedException;
import org.apache.streams.core.StreamsDatum;
import org.apache.streams.core.StreamsProcessor;
import org.apache.streams.jackson.StreamsJacksonMapper;
import org.apache.streams.pojo.json.Activity;
import org.json.JSONObject;
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

public class TikaProcessor implements StreamsProcessor
{
    private final static String STREAMS_ID = "LinkExpanderProcessor";

    private final static Logger LOGGER = LoggerFactory.getLogger(TikaProcessor.class);

    private ObjectMapper mapper;

    @Override
    public List<StreamsDatum> process(StreamsDatum entry) {

        List<StreamsDatum> result = Lists.newArrayList();

        LOGGER.debug("{} processing {}", STREAMS_ID, entry.getDocument().getClass());

        // get list of shared urls
        if( entry.getDocument() instanceof Activity) {

            Activity input = (Activity) entry.getDocument();

            List<String> outputLinks = input.getLinks();
            // for each
            for( String link : outputLinks ) {
                if( link instanceof String ) {
                    // expand
                    try {
                        StreamsDatum outputDatum = expandLink((String) link, entry);
                        result.add(outputDatum);
                    } catch (Exception e) {
                        //drop unexpandable links
                        LOGGER.debug("Failed to expand link : {}", link);
                        LOGGER.debug("Excpetion expanding link : {}", e);
                    }
                }
                else {
                    LOGGER.warn("Expected Links to be of type java.lang.String, but received {}", link.getClass().toString());
                }
            }


        }
        else if(entry.getDocument() instanceof String) {
            StreamsDatum outputDatum = expandLink((String) entry.getDocument(), entry);
            result.add(outputDatum);
        }
        else throw new NotImplementedException();

        return result;
    }

    private StreamsDatum expandLink(String link, StreamsDatum input) {

        LinkExpander expander = new LinkExpander((String)link);
        expander.run();
        StreamsDatum datum = null;
        if(input.getId() == null)
            datum = new StreamsDatum(this.mapper.convertValue(expander, JSONObject.class).toString(), expander.getFinalURL());
        else
            datum = new StreamsDatum(this.mapper.convertValue(expander, JSONObject.class).toString(), input.getId());
        datum.setSequenceid(input.getSequenceid());
        datum.setMetadata(input.getMetadata());
        datum.setTimestamp(input.getTimestamp());
        return datum;

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