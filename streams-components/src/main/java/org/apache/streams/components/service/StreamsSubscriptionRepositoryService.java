package org.apache.streams.components.service;

import org.apache.streams.persistence.model.ActivityStreamsSubscription;

import java.util.List;

public interface StreamsSubscriptionRepositoryService{
    void saveSubscription(ActivityStreamsSubscription subscription);
    List<ActivityStreamsSubscription> getAllSubscriptions();
}
