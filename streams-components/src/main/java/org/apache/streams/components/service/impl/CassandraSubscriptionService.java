package org.apache.streams.components.service.impl;

import org.apache.streams.components.service.StreamsSubscriptionRepositoryService;
import org.apache.streams.persistence.model.ActivityStreamsSubscription;
import org.apache.streams.persistence.repository.SubscriptionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

@Component
public class CassandraSubscriptionService implements StreamsSubscriptionRepositoryService {

    private SubscriptionRepository repository;

    @Autowired
    public CassandraSubscriptionService(SubscriptionRepository repository) {
        this.repository = repository;
    }

    @Override
    public void saveSubscription(ActivityStreamsSubscription subscription) {
        repository.save(subscription);
    }

    @Override
    public List<ActivityStreamsSubscription> getAllSubscriptions(){
        return repository.getAllSubscriptions();
    }

    @Override
    public void updateTags(String subscriberId, Set<String> add, Set<String> remove){
        repository.updateTags(subscriberId, add, remove);
    }

    @Override
    public ActivityStreamsSubscription getSubscriptionByUsername(String username){
        return repository.getSubscriptionByUsername(username);
    }

    @Override
    public ActivityStreamsSubscription getSubscriptionByInRoute(String inRoute){
        return repository.getSubscriptionByInRoute(inRoute);
    }

}
