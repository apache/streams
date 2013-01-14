package org.apache.streams.messaging.rules;


public interface RoutingRule {

    void setSource(String source);
    String getSource();
    void setDestination(String destination);
    String getDestination();
    boolean isMessageRoutable(String body);
}
