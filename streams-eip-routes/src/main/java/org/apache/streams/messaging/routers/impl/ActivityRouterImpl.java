package org.apache.streams.messaging.routers.impl;

import org.apache.streams.messaging.routers.ActivityRouter;

import java.lang.String;
import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import org.apache.streams.messaging.rules.RoutingRule;

import org.apache.camel.Properties;
import org.apache.streams.messaging.rules.impl.SimpleRoutingRule;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


public class ActivityRouterImpl implements ActivityRouter {
    private static final transient Log LOG = LogFactory.getLog(ActivityRouterImpl.class);

    private ArrayList<RoutingRule> routingRules;

    public ActivityRouterImpl(){
        routingRules = new ArrayList<RoutingRule>();
    }

    public ActivityRouterImpl(HashMap<String, String> routes){
        LOG.info(">> setting up routes >>");
       addRoutingRules(routes);
    }

    public String slip(String body, @Properties Map<String, Object> properties) {


        // get the state from the exchange properties and keep track how many times
        // we have been invoked
        int invoked = 0;
        Object current = properties.get("invoked");
        if (current != null) {
            invoked = Integer.valueOf(current.toString());
        }
        invoked++;
        // and store the state back on the properties
        properties.put("invoked", invoked);

        for(RoutingRule r : routingRules){
           if (r.isMessageRoutable(body)){
               return r.getDestination();
           }
        }

        // no more so return null
        return "direct:foo";
    }

    public void addRoutingRules(@Properties Map<String,String> rules){
        for(String key : rules.keySet()){
            LOG.info(key + ">> setting up routes >>" + rules.get(key));
            routingRules.add(new SimpleRoutingRule(key,rules.get(key)));
        }

    }

}
