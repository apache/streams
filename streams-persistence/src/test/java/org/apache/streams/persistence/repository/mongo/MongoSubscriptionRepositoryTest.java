package org.apache.streams.persistence.repository.mongo;

import org.apache.streams.persistence.configuration.MongoConfiguration;
import org.apache.streams.persistence.model.ActivityStreamsSubscription;
import org.apache.streams.persistence.model.mongo.MongoSubscription;
import org.apache.streams.persistence.repository.SubscriptionRepository;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MongoSubscriptionRepositoryTest {

    private SubscriptionRepository repository;
    private MongoConfiguration configuration;
    private MongoDatabase database;

    @Before
    public void setup() {
        configuration = new MongoConfiguration();
        configuration.setDbName("testdb1");
        configuration.setSubscriptionCollectionName("subscriptiontest2");
        database = new MongoDatabase(configuration);
        repository = new MongoSubscriptionRepository(database, configuration);
    }

    @Ignore
    @Test
    public void getAllSubscriptionsTest() {
        List<ActivityStreamsSubscription> subscriptions = repository.getAllSubscriptions();
    }

    @Ignore
    @Test
    public void getSubscriptionByUsernameTest() {
        ActivityStreamsSubscription subscription = repository.getSubscriptionByUsername("newID2");
    }

    @Ignore
    @Test
    public void getSubscriptionByInRouteTest() {
        ActivityStreamsSubscription subscription = repository.getSubscriptionByInRoute("randomID2");
    }

    @Ignore
    @Test
    public void addSubscriptionsTest() {
        ActivityStreamsSubscription subscription = new MongoSubscription();
        subscription.setUsername("newID3");
        subscription.setInRoute("randomID3");

        repository.save(subscription);
    }

    @Ignore
    @Test
    public void updateFiltersTest() {
        Set<String> add = new HashSet<String>(Arrays.asList("three","six"));
        Set<String> remove = new HashSet<String>(Arrays.asList("eight","seven"));


        repository.updateFilters("randomID3", add, remove);
    }
}
