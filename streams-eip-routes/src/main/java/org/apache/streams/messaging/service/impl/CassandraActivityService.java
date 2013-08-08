package org.apache.streams.messaging.service.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.camel.Exchange;
import org.apache.streams.cassandra.model.CassandraActivityStreamsEntry;
import org.apache.streams.cassandra.repository.impl.CassandraActivityStreamsRepository;
import org.apache.streams.messaging.service.ActivityService;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.ObjectMapper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CassandraActivityService implements ActivityService {

    private static final transient Log LOG = LogFactory.getLog(CassandraActivityService.class);

    private CassandraActivityStreamsRepository cassandraActivityStreamsRepository;
    private ObjectMapper mapper;

    public CassandraActivityService() {
        this.cassandraActivityStreamsRepository = new CassandraActivityStreamsRepository();
        this.mapper = new ObjectMapper();
        mapper.configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

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
                CassandraActivityStreamsEntry streamsEntry = mapper.readValue(activityJson, CassandraActivityStreamsEntry.class);
                cassandraActivityStreamsRepository.save(streamsEntry);
            } catch (IOException err) {
                LOG.error("there was an error while converting the json string to an object and saving to the database", err);
            }

        }
    }

    public List<String> getActivitiesForQuery(String query) {
        List<CassandraActivityStreamsEntry> activityObjects = cassandraActivityStreamsRepository.getActivitiesForQuery(query);
        return getJsonList(activityObjects);
    }

    private List<String> getJsonList(List<CassandraActivityStreamsEntry> activities) {
        List<String> jsonList = new ArrayList<String>();
        for (CassandraActivityStreamsEntry entry : activities) {
            try {
                jsonList.add(mapper.writeValueAsString(entry));
            } catch (IOException e) {
                LOG.error("There was an error while trying to convert the java object to a string: " + entry, e);
            }
        }
        return jsonList;
    }
}
