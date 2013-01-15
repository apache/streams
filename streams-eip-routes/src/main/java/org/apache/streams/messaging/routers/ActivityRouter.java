package org.apache.streams.messaging.routers;

import java.lang.Object;
import java.lang.String;
import java.util.ArrayList;
import java.util.Map;

import org.apache.camel.Exchange;
import org.apache.camel.Header;
import org.apache.camel.Properties;
import org.apache.streams.messaging.rules.RoutingRule;


public interface ActivityRouter {

    /*
    * Use this method to compute dynamic where we should route next.
    *
    * @param body the message body
    * @return endpoints to go, or <tt>null</tt> to indicate the end
    */
    String slip(String body,@Header(Exchange.SLIP_ENDPOINT) String previous);
    void addRoutingRules(ArrayList<RoutingRule> rules);

}
