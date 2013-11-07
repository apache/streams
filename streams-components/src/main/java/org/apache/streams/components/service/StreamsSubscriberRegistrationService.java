package org.apache.streams.components.service;

import java.io.IOException;

public interface StreamsSubscriberRegistrationService {
    String register(String subscriberJSON) throws Exception;
}
