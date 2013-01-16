package org.apache.streams.messaging.routers;

import java.lang.Object;
import java.lang.String;
import java.util.ArrayList;
import java.util.Map;

import org.apache.camel.Exchange;
import org.apache.camel.Header;
import org.apache.camel.Properties;
import org.apache.streams.messaging.rules.RoutingRule;
import org.apache.streams.osgi.components.activityconsumer.ActivityConsumer;


public interface ActivityRouter {

    String slip(Exchange exchange, String body,@Header("SRC") String src);
    void createNewRouteForConsumer(ActivityConsumer activityConsumer);

}
