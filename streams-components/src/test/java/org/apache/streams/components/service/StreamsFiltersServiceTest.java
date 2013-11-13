package org.apache.streams.components.service;

import org.apache.streams.components.activitysubscriber.ActivityStreamsSubscriber;
import org.apache.streams.components.activitysubscriber.ActivityStreamsSubscriberWarehouse;
import org.apache.streams.components.activitysubscriber.impl.ActivityStreamsSubscriberDelegate;
import org.apache.streams.components.service.impl.StreamsFiltersServiceImpl;
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

public class StreamsFiltersServiceTest {
    private StreamsFiltersService filtersService;
    private StreamsSubscriptionRepositoryService repositoryService;
    private ActivityStreamsSubscriberWarehouse subscriberWarehouse;

    @Before
    public void setup(){
        repositoryService = createMock(StreamsSubscriptionRepositoryService.class);
        subscriberWarehouse = createMock(ActivityStreamsSubscriberWarehouse.class);
        filtersService = new StreamsFiltersServiceImpl(repositoryService, subscriberWarehouse);
    }

    @Test
    public void updateFiltersTest() throws Exception {
        String subscriberId = "subscriberId";
        String tagsJson = "{\"add\":[\"this\"], \"remove\":[\"that\"]}";
        ActivityStreamsSubscription subscription = createMock(ActivityStreamsSubscription.class);
        ActivityStreamsSubscriber subscriber = new ActivityStreamsSubscriberDelegate();

        repositoryService.updateFilters(eq(subscriberId), isA(Set.class), isA(Set.class));
        expectLastCall();
        expect(repositoryService.getSubscriptionByInRoute(subscriberId)).andReturn(subscription);
        expect(subscriberWarehouse.getSubscriber(subscriberId)).andReturn(subscriber);
        subscriberWarehouse.updateSubscriber(subscription);
        expectLastCall();
        replay(repositoryService,subscriberWarehouse);

        String returned = filtersService.updateFilters(subscriberId,tagsJson);

        assertThat(returned,is(equalTo("Filters Updated Successfully!")));
        assertThat(subscriber.getLastUpdated().getTime(),is(equalTo(0L)));
    }

    @Test
    public void getTagsTest() throws Exception {
        String subscriberId = "subscriberId";
        ActivityStreamsSubscription subscription = createMock(ActivityStreamsSubscription.class);
        Set<String> filters = new HashSet<String>(Arrays.asList("tags"));

        expect(repositoryService.getSubscriptionByInRoute(subscriberId)).andReturn(subscription);
        expect(subscription.getFilters()).andReturn(filters);
        replay(subscription,repositoryService);

        String returned = filtersService.getFilters(subscriberId);

        assertThat(returned, is(equalTo("[\"tags\"]")));
    }
}
