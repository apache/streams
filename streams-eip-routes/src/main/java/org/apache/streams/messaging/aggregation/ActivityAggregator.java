package org.apache.streams.messaging.aggregation;


import org.apache.camel.Exchange;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.streams.cassandra.model.CassandraActivityStreamsEntry;
import org.apache.streams.osgi.components.activitysubscriber.ActivityStreamsSubscriber;
import org.apache.streams.osgi.components.activitysubscriber.ActivityStreamsSubscriberWarehouse;
import org.codehaus.jackson.map.ObjectMapper;

import java.io.IOException;
import java.util.List;

public class ActivityAggregator {

    private ActivityStreamsSubscriberWarehouse activityStreamsSubscriberWarehouse;
    private static final transient Log LOG = LogFactory.getLog(ActivityAggregator.class);

    public void setActivityStreamsSubscriberWarehouse(ActivityStreamsSubscriberWarehouse activityStreamsSubscriberWarehouse) {
        this.activityStreamsSubscriberWarehouse = activityStreamsSubscriberWarehouse;
    }

    public void distributeToSubscribers(Exchange exchange) {

        //iterate over the aggregated messages and send to subscribers in warehouse...they will evaluate and determine if they keep it
        List<Exchange> grouped = exchange.getProperty(Exchange.GROUPED_EXCHANGE, List.class);

        //initialize the ObjectMapper
        ObjectMapper mapper = new ObjectMapper();

        for (Exchange e : grouped){
            //get activity off of exchange
           LOG.info("Processing the activity...");
           LOG.info("Exchange: "+e);

            try {
                //extract the ActivityStreamsEntry object and translate to JSON
                CassandraActivityStreamsEntry activity = e.getIn().getBody(CassandraActivityStreamsEntry.class);
                String activityJSON = mapper.writeValueAsString(activity);
                LOG.info("Activity Object: "+activityJSON);
                for(ActivityStreamsSubscriber subscriber:activityStreamsSubscriberWarehouse.getAllSubscribers()){
                    subscriber.receive(activityJSON);
                }
            } catch (IOException err) {
                LOG.warn("There was an error translating the java object to JSON");
                LOG.warn(err);
            }
        }
    }
}