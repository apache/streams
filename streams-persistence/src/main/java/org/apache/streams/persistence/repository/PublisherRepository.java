package org.apache.streams.persistence.repository;

import org.apache.streams.persistence.model.ActivityStreamsPublisher;

public interface PublisherRepository {
    ActivityStreamsPublisher getPublisher(String inRoute);
    void save(ActivityStreamsPublisher publisher);
}
