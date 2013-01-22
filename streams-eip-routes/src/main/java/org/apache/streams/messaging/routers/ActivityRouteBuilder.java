package org.apache.streams.messaging.routers;



import org.apache.camel.Exchange;
import org.apache.streams.osgi.components.activityconsumer.ActivityConsumer;


public interface ActivityRouteBuilder {


    void createNewRouteForConsumer(Exchange exchange, ActivityConsumer activityConsumer);

}
