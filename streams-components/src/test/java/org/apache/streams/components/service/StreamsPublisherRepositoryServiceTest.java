package org.apache.streams.components.service;

import org.apache.streams.components.service.impl.CassandraPublisherService;
import org.apache.streams.persistence.model.ActivityStreamsPublisher;
import org.apache.streams.persistence.model.cassandra.CassandraPublisher;
import org.apache.streams.persistence.repository.PublisherRepository;
import org.junit.Before;
import org.junit.Test;

import static org.easymock.EasyMock.*;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

public class StreamsPublisherRepositoryServiceTest {

    private StreamsPublisherRepositoryService publisherRepositoryService;
    private PublisherRepository repository;

    @Before
    public void setup(){
        repository = createMock(PublisherRepository.class);
        publisherRepositoryService = new CassandraPublisherService(repository);
    }

    @Test
    public void savePublisherTest(){
        ActivityStreamsPublisher publisher = new CassandraPublisher();

        repository.save(publisher);
        expectLastCall();

        replay(repository);

        publisherRepositoryService.savePublisher(publisher);

        assertNotNull(publisher.getId());
        verify(repository);
    }

    @Test
    public void getActivityStreamsPublisherBySrcTest(){
        String src = "dis be ma src";
        ActivityStreamsPublisher publisher = createMock(ActivityStreamsPublisher.class);

        expect(repository.getPublisherBySrc(src)).andReturn(publisher);
        replay(repository);

        ActivityStreamsPublisher returned = publisherRepositoryService.getActivityStreamsPublisherBySrc(src);

        assertThat(publisher,sameInstance(returned));
    }

    @Test
    public void getActivityStreamsPublisherByInRoute(){
        String inRoute = "dis be ma inRoute";
        ActivityStreamsPublisher publisher = createMock(ActivityStreamsPublisher.class);

        expect(repository.getPublisherByInRoute(inRoute)).andReturn(publisher);
        replay(repository);

        ActivityStreamsPublisher returned = publisherRepositoryService.getActivityStreamsPublisherByInRoute(inRoute);

        assertThat(publisher,sameInstance(returned));
    }
}
