package org.apache.streams.messaging.routers.impl;


import org.apache.streams.messaging.routers.ActivityRouter;

import java.lang.String;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.ArrayList;
import org.apache.streams.messaging.rules.RoutingRule;


import org.apache.camel.Header;
import org.apache.camel.Exchange;

import org.apache.streams.messaging.rules.impl.SimpleRoutingRule;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


public class ActivityRouterImpl  implements ActivityRouter {
    private static final transient Log LOG = LogFactory.getLog(ActivityRouterImpl.class);

    private HashMap<String, RoutingRule> routingRules;

    public ActivityRouterImpl(){
        routingRules = new HashMap<String, RoutingRule>();
    }

    public ActivityRouterImpl(ArrayList<RoutingRule> routes){
        LOG.info(">> setting up routes >>");
        routingRules = new HashMap<String, RoutingRule>();
       addRoutingRules(routes);
    }



    public String slip(String body,@Header(Exchange.SLIP_ENDPOINT) String previous) {

        LOG.info("previous: " + previous);
            if (routingRules.containsKey(previous) ){
                return routingRules.get(previous).getDestination();
            }
        return null;


    }

    public void addRoutingRules(ArrayList<RoutingRule> routes){
        String previous = null;
        //previous endpoint will be the key lookup to maintain routing order
        for(RoutingRule r : routes){

            LOG.info(previous + ">> setting up routes >>" + r.getDestination());
            routingRules.put(previous, new SimpleRoutingRule(r.getSource(),r.getDestination()));
            previous = r.getDestination();
        }

    }

}
