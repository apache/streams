package org.apache.streams.components.service;

import org.apache.streams.components.activitysubscriber.ActivityStreamsSubscriberWarehouse;
import org.apache.streams.components.service.impl.StreamsSubscriberRegistrationServiceImpl;
import org.apache.streams.persistence.model.ActivityStreamsSubscription;
import org.junit.Before;
import org.junit.Test;

import static org.easymock.EasyMock.*;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class StreamsSubscriberRegistrationServiceTest {
    private StreamsSubscriberRegistrationService subscriberRegistrationService;
    private StreamsSubscriptionRepositoryService subscriptionRepositoryService;
    private ActivityStreamsSubscriberWarehouse activityStreamsSubscriberWarehouse;

    @Before
    public void setup(){
        subscriptionRepositoryService = createMock(StreamsSubscriptionRepositoryService.class);
        activityStreamsSubscriberWarehouse = createMock(ActivityStreamsSubscriberWarehouse.class);

        subscriberRegistrationService = new StreamsSubscriberRegistrationServiceImpl(subscriptionRepositoryService,activityStreamsSubscriberWarehouse);
    }

    @Test
    public void registerTest_inDB() throws Exception {
        String username = "blah";
        String inRoute = "inRoute";
        String subscriberJson = "{\"username\":\"blah\"}";

        ActivityStreamsSubscription subscription = createMock(ActivityStreamsSubscription.class);
        expect(subscriptionRepositoryService.getSubscriptionByUsername(username)).andReturn(subscription);
        expect(subscription.getInRoute()).andReturn(inRoute);
        replay(subscriptionRepositoryService, subscription);

        String returned = subscriberRegistrationService.register(subscriberJson);

        assertThat(returned, is(equalTo(inRoute)));
    }

    @Test
    public void registerTest_notInDB() throws Exception {
        String username = "blah";
        String subscriberJson = "{\"username\":\"blah\"}";

        expect(subscriptionRepositoryService.getSubscriptionByUsername(username)).andReturn(null);
        subscriptionRepositoryService.saveSubscription(isA(ActivityStreamsSubscription.class));
        expectLastCall();
        activityStreamsSubscriberWarehouse.register(isA(ActivityStreamsSubscription.class));
        expectLastCall();
        replay(subscriptionRepositoryService, activityStreamsSubscriberWarehouse);

        String returned = subscriberRegistrationService.register(subscriberJson);

        assertThat(returned, is(instanceOf(String.class)));
        verify(subscriptionRepositoryService,activityStreamsSubscriberWarehouse);
    }
}
