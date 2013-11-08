package org.apache.streams.components.service;

import org.apache.streams.components.activitysubscriber.ActivityStreamsSubscriberWarehouse;
import org.apache.streams.components.service.impl.StreamsTagsUpdatingServiceImpl;
import org.apache.streams.persistence.model.ActivityStreamsSubscription;
import org.junit.Before;
import org.junit.Test;

import java.util.Set;

import static org.easymock.EasyMock.*;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;

public class StreamsTagsUpdatingServiceTest {

    private StreamsTagsUpdatingService tagsUpdatingService;
    private StreamsSubscriptionRepositoryService repositoryService;
    private ActivityStreamsSubscriberWarehouse subscriberWarehouse;

    @Before
    public void setup(){
        repositoryService = createMock(StreamsSubscriptionRepositoryService.class);
        subscriberWarehouse = createMock(ActivityStreamsSubscriberWarehouse.class);
        tagsUpdatingService = new StreamsTagsUpdatingServiceImpl(repositoryService, subscriberWarehouse);
        }

    @Test
    public void updateTagsTest() throws Exception {
        String subscriberId = "subscriberId";
        String tagsJson = "{\"add\":[\"this\"], \"remove\":[\"that\"]}";
        ActivityStreamsSubscription subscription = createMock(ActivityStreamsSubscription.class);

        repositoryService.updateTags(eq(subscriberId), isA(Set.class), isA(Set.class));
        expectLastCall();
        expect(repositoryService.getSubscriptionByInRoute(subscriberId)).andReturn(subscription);
        subscriberWarehouse.updateSubscriber(subscription);
        expectLastCall();
        replay(repositoryService,subscriberWarehouse);

        String returned = tagsUpdatingService.updateTags(subscriberId,tagsJson);

        assertThat(returned,is(equalTo("Tags Updated Successfully!")));
    }
}
