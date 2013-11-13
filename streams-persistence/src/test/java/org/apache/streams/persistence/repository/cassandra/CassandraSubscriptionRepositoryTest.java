package org.apache.streams.persistence.repository.cassandra;

import org.apache.streams.persistence.configuration.CassandraConfiguration;
import org.apache.streams.persistence.model.ActivityStreamsSubscription;
import org.apache.streams.persistence.model.cassandra.CassandraSubscription;
import org.apache.streams.persistence.repository.SubscriptionRepository;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CassandraSubscriptionRepositoryTest {

    private SubscriptionRepository repository;

    @Before
    public void setup() {
        CassandraConfiguration configuration = new CassandraConfiguration();
        configuration.setCassandraHost("127.0.0.1");
        configuration.setCassandraPort(9042);
        configuration.setSubscriptionColumnFamilyName("subscriptionstestA");
        configuration.setKeyspaceName("keyspacetest");
        CassandraKeyspace keyspace = new CassandraKeyspace(configuration);

        repository = new CassandraSubscriptionRepository(keyspace, configuration);
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
        ActivityStreamsSubscription subscription = new CassandraSubscription();
        subscription.setUsername("newID2");
        subscription.setInRoute("randomID2");

        repository.save(subscription);
    }

    @Ignore
    @Test
    public void updateTagsTest() {
        ActivityStreamsSubscription subscription = new CassandraSubscription();
        subscription.setInRoute("randomID");

        Set<String> add = new HashSet<String>(Arrays.asList("five","six"));
        Set<String> remove = new HashSet<String>(Arrays.asList("one","three"));


        repository.updateFilters("randomID2", add, remove);
    }
}
