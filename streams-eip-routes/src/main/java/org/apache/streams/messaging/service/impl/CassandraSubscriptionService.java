package org.apache.streams.messaging.service.impl;

import org.apache.streams.cassandra.repository.impl.CassandraSubscriptionRepository;
import org.apache.streams.messaging.service.SubscriptionService;
import org.apache.streams.osgi.components.activitysubscriber.ActivityStreamsSubscription;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
public class CassandraSubscriptionService implements SubscriptionService {

    private CassandraSubscriptionRepository repository;

    @Autowired
    public CassandraSubscriptionService(CassandraSubscriptionRepository repository){
        this.repository = repository;
    }

    public List<String> getFilters(String authToken){
          return Arrays.asList(repository.getFilters(authToken).split(" "));
    }

    public void saveFilters(ActivityStreamsSubscription subscription){
          repository.save(subscription);
    }
}
