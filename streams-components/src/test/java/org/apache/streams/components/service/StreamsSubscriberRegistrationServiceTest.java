package org.apache.streams.components.service;

import org.apache.streams.components.activitysubscriber.ActivityStreamsSubscriberWarehouse;
import org.apache.streams.components.service.impl.StreamsSubscriberRegistrationServiceImpl;
import org.apache.streams.persistence.model.ActivityStreamsSubscription;
import org.apache.streams.persistence.repository.SubscriptionRepository;
import org.junit.Before;
import org.junit.Test;

import static org.easymock.EasyMock.*;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class StreamsSubscriberRegistrationServiceTest {
    private StreamsSubscriberRegistrationService subscriberRegistrationService;
    private SubscriptionRepository subscriptionRepository;
    private ActivityStreamsSubscriberWarehouse activityStreamsSubscriberWarehouse;

    @Before
    public void setup(){
        subscriptionRepository = createMock(SubscriptionRepository.class);
        activityStreamsSubscriberWarehouse = createMock(ActivityStreamsSubscriberWarehouse.class);

        subscriberRegistrationService = new StreamsSubscriberRegistrationServiceImpl(subscriptionRepository,activityStreamsSubscriberWarehouse);
    }

    @Test
    public void registerTest_inDB() throws Exception {
        String username = "blah";
        String inRoute = "inRoute";
        String subscriberJson = "{\"username\":\"blah\"}";

        ActivityStreamsSubscription subscription = createMock(ActivityStreamsSubscription.class);
        expect(subscriptionRepository.getSubscriptionByUsername(username)).andReturn(subscription);
        expect(subscription.getInRoute()).andReturn(inRoute);
        replay(subscriptionRepository, subscription);

        String returned = subscriberRegistrationService.register(subscriberJson);

        assertThat(returned, is(equalTo(inRoute)));
    }

    @Test
    public void registerTest_notInDB() throws Exception {
        String username = "blah";
        String subscriberJson = "{\"username\":\"blah\"}";

        expect(subscriptionRepository.getSubscriptionByUsername(username)).andReturn(null);
        subscriptionRepository.save(isA(ActivityStreamsSubscription.class));
        expectLastCall();
        activityStreamsSubscriberWarehouse.register(isA(ActivityStreamsSubscription.class));
        expectLastCall();
        replay(subscriptionRepository, activityStreamsSubscriberWarehouse);

        String returned = subscriberRegistrationService.register(subscriberJson);

        assertThat(returned, is(instanceOf(String.class)));
        verify(subscriptionRepository,activityStreamsSubscriberWarehouse);
    }
}
