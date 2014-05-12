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

public class LinkResolverProcessor implements StreamsProcessor {

    private static final String STREAMS_ID = "LinkResolverProcessor";
    private static final Logger LOGGER = LoggerFactory.getLogger(LinkResolverProcessor.class);
    private static ObjectMapper mapper = StreamsJacksonMapper.getInstance();

    @Override
    public List<StreamsDatum> process(StreamsDatum entry) {

        List<StreamsDatum> result = Lists.newArrayList();

        LOGGER.debug("{} processing {}", STREAMS_ID, entry.getDocument().getClass());

        Activity activity;

        // get list of shared urls
        if (entry.getDocument() instanceof Activity) {
            activity = (Activity) entry.getDocument();

            activity.setLinks(unwind(activity.getLinks()));

            entry.setDocument(activity);

            result.add(entry);

            return result;
        } else if (entry.getDocument() instanceof String) {

            try {
                activity = mapper.readValue((String) entry.getDocument(), Activity.class);
            } catch (Exception e) {
                e.printStackTrace();
                LOGGER.warn(e.getMessage());
                return (Lists.newArrayList(entry));
            }

            activity.setLinks(unwind(activity.getLinks()));

            try {
                entry.setDocument(mapper.writeValueAsString(activity));
            } catch (Exception e) {
                e.printStackTrace();
                LOGGER.warn(e.getMessage());
                return (Lists.newArrayList());
            }

            result.add(entry);

            return result;

        } else {
            //return(Lists.newArrayList(entry));
            return (Lists.newArrayList());
        }
    }

    @Override
    public void prepare(Object o) {
        // noOp
    }

    @Override
    public void cleanUp() {
        // noOp
    }


    protected List<String> unwind(List<String> inputLinks) {
        List<String> outputLinks = Lists.newArrayList();
        for (String link : inputLinks) {
            try {
                LinkResolver unwinder = new LinkResolver(link);
                unwinder.run();
                outputLinks.add(unwinder.getLinkDetails().getFinalURL());
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