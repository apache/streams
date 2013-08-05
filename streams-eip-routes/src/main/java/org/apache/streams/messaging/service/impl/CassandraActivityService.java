package org.apache.streams.messaging.service.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.camel.Exchange;
import org.apache.streams.cassandra.model.CassandraActivityStreamsEntry;
import org.apache.streams.cassandra.repository.impl.CassandraActivityStreamsRepository;
import org.apache.streams.messaging.service.ActivityService;
import org.codehaus.jackson.map.ObjectMapper;

import java.io.IOException;
import java.util.List;

public class CassandraActivityService implements ActivityService {

    private static final transient Log LOG = LogFactory.getLog(CassandraActivityService.class);

    private CassandraActivityStreamsRepository cassandraActivityStreamsRepository;

    public CassandraActivityService(){
        this.cassandraActivityStreamsRepository = new CassandraActivityStreamsRepository();
    }

    public void receiveExchange(Exchange exchange) {

        //receive the exchange as a list
        List<Exchange> grouped = exchange.getProperty(Exchange.GROUPED_EXCHANGE, List.class);

        //initialize the ObjectMapper
        ObjectMapper mapper = new ObjectMapper();

        for (Exchange e : grouped) {
            //get activity off of exchange
            LOG.info("Processing the activity...");
            LOG.info("Exchange: " + e);

            //extract the ActivityStreamsEntry object and save it in the database
            CassandraActivityStreamsEntry activity = e.getIn().getBody(CassandraActivityStreamsEntry.class);
            LOG.info("The activity object with id: " + activity.getId() + "was received from the exchange");
            cassandraActivityStreamsRepository.save(activity);
        }
    }
}
