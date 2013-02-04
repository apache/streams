package org.apache.streams.messaging.routers;



import org.apache.camel.Exchange;
import org.apache.streams.osgi.components.activitysubscriber.ActivityStreamsSubscriber;


public interface ActivityStreamsSubscriberRouteBuilder {


    void createNewRouteForSubscriber(Exchange exchange, ActivityStreamsSubscriber activityStreamsSubscriber);

}
