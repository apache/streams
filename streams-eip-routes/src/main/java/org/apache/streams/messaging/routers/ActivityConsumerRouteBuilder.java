package org.apache.streams.messaging.routers;



import org.apache.camel.Exchange;
import org.apache.streams.osgi.components.activityconsumer.ActivityConsumer;


public interface ActivityConsumerRouteBuilder {


    void createNewRouteForConsumer(Exchange exchange, ActivityConsumer activityConsumer);

}
