package org.apache.streams.components.service.impl;

import org.apache.streams.components.service.StreamsSubscriptionRepositoryService;
import org.apache.streams.persistence.model.ActivityStreamsSubscription;
import org.apache.streams.persistence.repository.SubscriptionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
public class CassandraSubscriptionService implements StreamsSubscriptionRepositoryService {

    private SubscriptionRepository repository;

    @Autowired
    public CassandraSubscriptionService(SubscriptionRepository repository){
        this.repository = repository;
    }

    public List<String> getFilters(String authToken){
          return Arrays.asList(repository.getFilters(authToken).split(" "));
    }

    public void saveFilters(ActivityStreamsSubscription subscription){
          //repository.save(subscription);
    }
}
