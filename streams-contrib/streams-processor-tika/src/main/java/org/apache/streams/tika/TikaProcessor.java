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

package org.apache.streams.tika;

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

public class TikaProcessor implements StreamsProcessor
{
    private final static String STREAMS_ID = "LinkCrawlerProcessor";

    private final static Logger LOGGER = LoggerFactory.getLogger(TikaProcessor.class);

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

        List<String> outputLinks = activity.getLinks();
        // for each
        for( String link : outputLinks ) {

            System.out.println( "pulling " + link);

            try {
                StreamsDatum outputDatum = expandLink(link, entry);
                if( outputDatum != null )
                    result.add(outputDatum);
            } catch (Exception e) {
                //drop unexpandable links
                LOGGER.debug("Failed to expand link : {}", link);
                LOGGER.debug("Excpetion expanding link : {}", e);
            }

        }

        return result;
    }

    private StreamsDatum expandLink(String link, StreamsDatum input) {

        LinkCrawler expander = new LinkCrawler((String)link);
        expander.run();
        StreamsDatum datum = null;
        if(input.getId() == null)
            try {
                datum = new StreamsDatum(this.mapper.writeValueAsString(expander.getArticle()), expander.getFinalURL());
            } catch (JsonProcessingException e) {
                e.printStackTrace();
                return null;
            }
        else
            try {
                datum = new StreamsDatum(this.mapper.writeValueAsString(expander.getArticle()), input.getId());
            } catch (JsonProcessingException e) {
                e.printStackTrace();
                return null;
            }
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