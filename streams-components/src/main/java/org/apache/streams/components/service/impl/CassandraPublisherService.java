package org.apache.streams.components.service.impl;

import org.apache.streams.components.service.StreamsPublisherRepositoryService;
import org.apache.streams.persistence.model.ActivityStreamsPublisher;
import org.apache.streams.persistence.repository.PublisherRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class CassandraPublisherService implements StreamsPublisherRepositoryService {
    private PublisherRepository repository;

    @Autowired
    public CassandraPublisherService(PublisherRepository repository) {
        this.repository = repository;
    }

    @Override
    public void savePublisher(ActivityStreamsPublisher publisher) {
        publisher.setId("" + UUID.randomUUID());
        repository.save(publisher);
    }

    @Override
    public ActivityStreamsPublisher getActivityStreamsPublisherBySrc(String src) {
        return repository.getPublisherBySrc(src);
    }

    @Override
    public ActivityStreamsPublisher getActivityStreamsPublisherByInRoute(String inRoute) {
        return repository.getPublisherByInRoute(inRoute);
    }


}
