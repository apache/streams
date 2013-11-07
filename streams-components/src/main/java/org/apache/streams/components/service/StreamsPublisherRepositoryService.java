package org.apache.streams.components.service;

import org.apache.streams.persistence.model.ActivityStreamsPublisher;

public interface StreamsPublisherRepositoryService {
    void savePublisher(ActivityStreamsPublisher publisher);
    ActivityStreamsPublisher getActivityStreamsPublisherBySrc(String src);
    ActivityStreamsPublisher getActivityStreamsPublisherByInRoute(String inRoute);
}
