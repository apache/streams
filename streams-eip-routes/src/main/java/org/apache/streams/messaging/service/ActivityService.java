package org.apache.streams.messaging.service;

import org.apache.camel.Exchange;

import java.util.List;

public interface ActivityService {

    void receiveExchange(Exchange exchange);

    List<String> getActivitiesForQuery(String query);
}
