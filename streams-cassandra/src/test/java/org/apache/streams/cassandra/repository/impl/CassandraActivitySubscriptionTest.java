package org.apache.streams.cassandra.repository.impl;

import org.apache.streams.osgi.components.activitysubscriber.ActivityStreamsSubscription;
import org.apache.streams.osgi.components.activitysubscriber.impl.ActivityStreamsSubscriptionImpl;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.util.Arrays;

public class CassandraActivitySubscriptionTest {

    public CassandraSubscriptionRepository repository;


    @Before
    public void setup() {
        repository = new CassandraSubscriptionRepository();
    }

    @Ignore
    @Test
    public void saveTest(){
        ActivityStreamsSubscription subscription = new ActivityStreamsSubscriptionImpl();
        subscription.setFilters(Arrays.asList("thisis", "atest"));
        subscription.setAuthToken("subid");

        repository.save(subscription);
    }

    @Ignore
    @Test
    public void getTest(){
        String filters = repository.getFilters("subid");
    }
}
