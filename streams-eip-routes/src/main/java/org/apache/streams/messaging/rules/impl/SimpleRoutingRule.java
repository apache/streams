package org.apache.streams.messaging.rules.impl;

import org.apache.streams.messaging.rules.RoutingRule;

public class SimpleRoutingRule implements RoutingRule{
    private String source;
    private String destination;

    public SimpleRoutingRule(String source, String destination){
        setSource(source);
        setDestination(destination);
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }


    public boolean isMessageRoutable(String body){
        if (body!=null){  return true; }
        return false;
    }

}
