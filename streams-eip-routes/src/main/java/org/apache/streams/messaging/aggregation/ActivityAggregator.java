package org.apache.streams.messaging.aggregation;


import org.apache.camel.Exchange;
import org.apache.camel.processor.aggregate.AggregationStrategy;
import org.apache.streams.osgi.components.activitysubscriber.ActivityStreamsSubscriber;
import org.apache.streams.osgi.components.activitysubscriber.ActivityStreamsSubscriberWarehouse;

import java.util.List;

public class ActivityAggregator {

    private ActivityStreamsSubscriberWarehouse activityStreamsSubscriberWarehouse;

    public void setActivityStreamsSubscriberWarehouse(ActivityStreamsSubscriberWarehouse activityStreamsSubscriberWarehouse) {
        this.activityStreamsSubscriberWarehouse = activityStreamsSubscriberWarehouse;
    }

    public void distributeToSubscribers(Exchange exchange) {

        //iterate over the aggregated messages and send to subscribers in warehouse...they will evaluate and determine if they keep it
        List<Exchange> grouped = exchange.getProperty(Exchange.GROUPED_EXCHANGE, List.class);

        for (Exchange e : grouped){
            //get activity off of exchange
           String activity= e.getIn().getBody(String.class);

            for(ActivityStreamsSubscriber subscriber:activityStreamsSubscriberWarehouse.getAllSubscribers()){
                subscriber.receive(activity);
            }
        }
    }
}