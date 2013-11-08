package org.apache.streams.components.service;

import org.apache.streams.components.activitysubscriber.ActivityStreamsSubscriberWarehouse;
import org.apache.streams.components.service.impl.StreamsTagsServiceImpl;
import org.apache.streams.persistence.model.ActivityStreamsSubscription;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static org.easymock.EasyMock.*;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class StreamsTagsServiceTest {
    private StreamsTagsService tagsService;
    private StreamsSubscriptionRepositoryService repositoryService;
    private ActivityStreamsSubscriberWarehouse subscriberWarehouse;

    @Before
    public void setup(){
        repositoryService = createMock(StreamsSubscriptionRepositoryService.class);
        subscriberWarehouse = createMock(ActivityStreamsSubscriberWarehouse.class);
        tagsService = new StreamsTagsServiceImpl(repositoryService, subscriberWarehouse);
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

        String returned = tagsService.updateTags(subscriberId,tagsJson);

        assertThat(returned,is(equalTo("Tags Updated Successfully!")));
    }

    @Test
    public void getTagsTest() throws Exception {
        String subscriberId = "subscriberId";
        ActivityStreamsSubscription subscription = createMock(ActivityStreamsSubscription.class);
        Set<String> tags = new HashSet<String>(Arrays.asList("tags"));

        expect(repositoryService.getSubscriptionByInRoute(subscriberId)).andReturn(subscription);
        expect(subscription.getTags()).andReturn(tags);
        replay(subscription,repositoryService);

        String returned = tagsService.getTags(subscriberId);

        assertThat(returned, is(equalTo("[\"tags\"]")));
    }
}
