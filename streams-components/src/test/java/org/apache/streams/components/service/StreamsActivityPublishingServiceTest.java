package org.apache.streams.components.service;

import org.apache.streams.components.service.impl.StreamsActivityPublishingServiceImpl;
import org.apache.streams.persistence.model.ActivityStreamsPublisher;
import org.junit.Before;
import org.junit.Test;

import static org.easymock.EasyMock.*;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

public class StreamsActivityPublishingServiceTest {

    private StreamsActivityPublishingService activityPublishingService;
    private StreamsActivityRepositoryService activityService;
    private StreamsPublisherRepositoryService publisherService;

    @Before
    public void setup(){
        activityService = createMock(StreamsActivityRepositoryService.class);
        publisherService = createMock(StreamsPublisherRepositoryService.class);

        activityPublishingService = new StreamsActivityPublishingServiceImpl(activityService, publisherService);
    }

    @Test
    public void publishTest() throws Exception {
        String inRoute = "myInRoute";
        String activityJson = "myActionJson";
        ActivityStreamsPublisher publisher = createMock(ActivityStreamsPublisher.class);

        expect(publisherService.getActivityStreamsPublisherByInRoute(inRoute)).andReturn(publisher);
        activityService.receiveActivity(publisher, activityJson);
        expectLastCall();

        replay(publisherService,activityService);

        assertThat(activityJson, is(equalTo(activityPublishingService.publish(inRoute, activityJson))));
    }
}
