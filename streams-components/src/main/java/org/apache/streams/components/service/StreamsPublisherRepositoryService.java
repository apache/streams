package org.apache.streams.components.service;

import org.apache.streams.persistence.model.ActivityStreamsPublisher;

public interface StreamsPublisherRepositoryService {
    void savePublisher(ActivityStreamsPublisher publisher);
    ActivityStreamsPublisher getActivityStreamsPublisher(String inRoute);
}
