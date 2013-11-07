package org.apache.streams.components.service;

public interface StreamsSubscriberRegistrationService {
    String register(String subscriberJSON) throws Exception;
}
