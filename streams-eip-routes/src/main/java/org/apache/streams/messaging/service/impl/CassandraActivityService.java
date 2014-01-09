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

package org.apache.streams.messaging.service.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.camel.Exchange;
import org.apache.rave.model.ActivityStreamsEntry;
import org.apache.streams.cassandra.model.CassandraActivityStreamsEntry;
import org.apache.streams.cassandra.repository.impl.CassandraActivityStreamsRepository;
import org.apache.streams.messaging.service.ActivityService;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class CassandraActivityService implements ActivityService {

    private static final transient Log LOG = LogFactory.getLog(CassandraActivityService.class);

    private CassandraActivityStreamsRepository cassandraActivityStreamsRepository;
    private ObjectMapper mapper;

    @Autowired
    public CassandraActivityService(CassandraActivityStreamsRepository cassandraActivityStreamsRepository, ObjectMapper mapper) {
        this.cassandraActivityStreamsRepository = cassandraActivityStreamsRepository;
        this.mapper = mapper;
        mapper.configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    @Override
    public void receiveExchange(Exchange exchange) {

        //receive the exchange as a list
        List<Exchange> grouped = exchange.getProperty(Exchange.GROUPED_EXCHANGE, List.class);

        for (Exchange e : grouped) {
            //get activity off of exchange
            LOG.info("Exchange: " + e);

            //extract the ActivityStreamsEntry object and save it in the database
            LOG.info("About to preform the translation to JSON Object");
            String activityJson = e.getIn().getBody(String.class);

            try {
                ActivityStreamsEntry streamsEntry = mapper.readValue(activityJson, CassandraActivityStreamsEntry.class);
                streamsEntry.setPublished(new Date());
                cassandraActivityStreamsRepository.save(streamsEntry);
            } catch (IOException err) {
                LOG.error("there was an error while converting the json string to an object and saving to the database", err);
            }

        }
    }

    @Override
    public List<String> getActivitiesForFilters(List<String> filters, Date lastUpdated) {
        List<CassandraActivityStreamsEntry> activityObjects = cassandraActivityStreamsRepository.getActivitiesForFilters(filters, lastUpdated);
        Collections.sort(activityObjects, Collections.reverseOrder());
        //TODO: make the number of streams returned configurable
        return getJsonList(activityObjects.subList(0,Math.min(activityObjects.size(),10)));
    }

    private List<String> getJsonList(List<CassandraActivityStreamsEntry> activities) {
        List<String> jsonList = new ArrayList<String>();
        for (ActivityStreamsEntry entry : activities) {
            try {
                jsonList.add(mapper.writeValueAsString(entry));
            } catch (IOException e) {
                LOG.error("There was an error while trying to convert the java object to a string: " + entry, e);
            }
        }
        return jsonList;
    }
}
