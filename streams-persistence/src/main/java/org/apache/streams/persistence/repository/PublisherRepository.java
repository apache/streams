package org.apache.streams.persistence.repository;

import org.apache.streams.persistence.model.ActivityStreamsPublisher;

public interface PublisherRepository {
    ActivityStreamsPublisher getPublisherByInRoute(String inRoute);
    ActivityStreamsPublisher getPublisherBySrc(String src);
    void save(ActivityStreamsPublisher publisher);
}
