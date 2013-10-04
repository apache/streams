package org.apache.streams.components.service;

import org.codehaus.jackson.JsonParseException;

import java.io.IOException;

public interface StreamsSubscriberRegistrationService {
    String register(String subscriberJSON) throws IOException;
}
