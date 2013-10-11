package org.apache.streams.components.service;

import java.io.IOException;

public interface StreamsActivityPublishingService {
    String publish(String publisherID, String activityJSON) throws Exception;
}
