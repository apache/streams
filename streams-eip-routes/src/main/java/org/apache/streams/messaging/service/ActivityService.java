package org.apache.streams.messaging.service;

import org.apache.camel.Exchange;

import java.io.IOException;
import java.util.Date;
import java.util.List;

public interface ActivityService {

    void receiveExchange(Exchange exchange);
    void receiveActivity(String activityJSON) throws IOException;

    List<String> getActivitiesForFilters(List<String> filters, Date lastUpdated);
}
