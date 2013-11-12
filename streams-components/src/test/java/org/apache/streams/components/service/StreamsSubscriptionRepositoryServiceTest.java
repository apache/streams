package org.apache.streams.components.service;

import org.apache.streams.components.service.impl.CassandraSubscriptionService;
import org.apache.streams.persistence.model.ActivityStreamsSubscription;
import org.apache.streams.persistence.repository.SubscriptionRepository;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.easymock.EasyMock.*;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;

public class StreamsSubscriptionRepositoryServiceTest {
    private StreamsSubscriptionRepositoryService subscriptionRepositoryService;
    private SubscriptionRepository repository;

    @Before
    public void setup(){
        repository = createMock(SubscriptionRepository.class);
        subscriptionRepositoryService = new CassandraSubscriptionService(repository);
    }

    @Test
    public void saveSubscriptionTest(){
        ActivityStreamsSubscription subscription = createMock(ActivityStreamsSubscription.class);
        repository.save(subscription);
        expectLastCall();
        replay(repository);

        subscriptionRepositoryService.saveSubscription(subscription);

        verify(repository);
    }

    @Test
    public void getAllSubscriptionsTest(){
        List<ActivityStreamsSubscription> subscriptionList = Arrays.asList(createMock(ActivityStreamsSubscription.class));
        expect(repository.getAllSubscriptions()).andReturn(subscriptionList);
        replay(repository);

        List<ActivityStreamsSubscription> returned = subscriptionRepositoryService.getAllSubscriptions();

        assertThat(returned, is(sameInstance(subscriptionList)));
    }

    @Test
    public void updateFiltersTest(){
        String subscriberId = "subscriberId";
        Set<String> add = new HashSet<String>(Arrays.asList("added"));
        Set<String> remove = new HashSet<String>(Arrays.asList("removed"));

        repository.updateFilters(subscriberId,add,remove);
        expectLastCall();
        replay(repository);

        subscriptionRepositoryService.updateFilters(subscriberId,add,remove);

        verify(repository);
    }

    @Test
    public void getSubscriptionByUsernameTest(){
        String username="username";
        ActivityStreamsSubscription subscription = createMock(ActivityStreamsSubscription.class);
        expect(repository.getSubscriptionByUsername(username)).andReturn(subscription);
        replay(repository);

        ActivityStreamsSubscription returned = subscriptionRepositoryService.getSubscriptionByUsername(username);
        assertThat(returned,is(sameInstance(subscription)));
    }

    @Test
    public void getSubscriptionByInRouteTest(){
        String inRoute="inRoute";
        ActivityStreamsSubscription subscription = createMock(ActivityStreamsSubscription.class);
        expect(repository.getSubscriptionByInRoute(inRoute)).andReturn(subscription);
        replay(repository);

        ActivityStreamsSubscription returned = subscriptionRepositoryService.getSubscriptionByInRoute(inRoute);
        assertThat(returned,is(sameInstance(subscription)));
    }
}
