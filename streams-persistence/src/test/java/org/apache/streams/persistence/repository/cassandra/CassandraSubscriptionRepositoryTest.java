package org.apache.streams.persistence.repository.cassandra;

import org.apache.streams.persistence.configuration.CassandraConfiguration;
import org.apache.streams.persistence.model.ActivityStreamsSubscription;
import org.apache.streams.persistence.model.cassandra.CassandraSubscription;
import org.apache.streams.persistence.repository.SubscriptionRepository;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

public class CassandraSubscriptionRepositoryTest {

    private SubscriptionRepository repository;

    @Before
    public void setup() {
        CassandraConfiguration configuration = new CassandraConfiguration();
        configuration.setCassandraPort("127.0.0.1");
        configuration.setSubscriptionColumnFamilyName("subscriptionsA");
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
    public void addSubscriptionsTest() {
        ActivityStreamsSubscription subscription = new CassandraSubscription();
        subscription.setFilters(Arrays.asList("r501", "tags"));
        subscription.setId("newID2");
        subscription.setInRoute("randomID");

        repository.save(subscription);
    }
}
