package org.apache.streams.messaging.rules;


public interface RoutingRule {

    void setSource(String source);
    String getSource();

    String getDestination();
    boolean isMessageRoutable(String body);
}
