package org.apache.streams.messaging.service;

import org.apache.camel.Exchange;

public interface ActivityService {

    void receiveExchange(Exchange exchange);
}
