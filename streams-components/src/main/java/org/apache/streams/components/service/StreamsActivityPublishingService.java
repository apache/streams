package org.apache.streams.components.service;

public interface StreamsActivityPublishingService {
    String publish(String publisherID, String activityJSON) throws Exception;
}
