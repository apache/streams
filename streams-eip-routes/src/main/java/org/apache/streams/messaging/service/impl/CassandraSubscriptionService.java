package org.apache.streams.messaging.service.impl;

import org.apache.streams.cassandra.repository.impl.CassandraSubscriptionRepository;
import org.apache.streams.messaging.service.SubscriptionService;
import org.apache.streams.osgi.components.activitysubscriber.ActivityStreamsSubscription;

import java.util.Arrays;
import java.util.List;

public class CassandraSubscriptionService implements SubscriptionService {

    private CassandraSubscriptionRepository repository;

    public CassandraSubscriptionService(){
        repository = new CassandraSubscriptionRepository();
    }

    public List<String> getFilters(String authToken){
          return Arrays.asList(repository.getFilters(authToken).split(" "));
    }

    public void saveFilters(ActivityStreamsSubscription subscription){
          repository.save(subscription);
    }
}
