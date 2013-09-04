package org.apache.streams.messaging.service;

import org.apache.camel.Exchange;

import java.util.Date;
import java.util.List;

public interface ActivityService {

    public void receiveExchange(Exchange exchange);

    public List<String> getActivitiesForFilters(List<String> filters, Date lastUpdated);
}
