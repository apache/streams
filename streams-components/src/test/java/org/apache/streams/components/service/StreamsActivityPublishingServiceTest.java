package org.apache.streams.components.service;

import org.apache.streams.components.service.impl.StreamsActivityPublishingServiceImpl;
import org.apache.streams.persistence.model.ActivityStreamsPublisher;
import org.apache.streams.persistence.repository.PublisherRepository;
import org.junit.Before;
import org.junit.Test;

import static org.easymock.EasyMock.*;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

public class StreamsActivityPublishingServiceTest {

    private StreamsActivityPublishingService activityPublishingService;
    private StreamsActivityRepositoryService activityService;
    private PublisherRepository publisherRepository;

    @Before
    public void setup(){
        activityService = createMock(StreamsActivityRepositoryService.class);
        publisherRepository = createMock(PublisherRepository.class);

        activityPublishingService = new StreamsActivityPublishingServiceImpl(activityService, publisherRepository);
    }

    @Test
    public void publishTest() throws Exception {
        String inRoute = "myInRoute";
        String activityJson = "myActionJson";
        ActivityStreamsPublisher publisher = createMock(ActivityStreamsPublisher.class);

        expect(publisherRepository.getPublisherByInRoute(inRoute)).andReturn(publisher);
        activityService.receiveActivity(activityJson);
        expectLastCall();

        replay(publisherRepository,activityService);

        assertThat(activityJson, is(equalTo(activityPublishingService.publish(inRoute, activityJson))));
    }
}
