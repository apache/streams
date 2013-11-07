package org.apache.streams.components.service;

import org.apache.streams.persistence.model.ActivityStreamsSubscription;

import java.util.List;
import java.util.Set;

public interface StreamsSubscriptionRepositoryService{
    void saveSubscription(ActivityStreamsSubscription subscription);
    List<ActivityStreamsSubscription> getAllSubscriptions();
    void updateTags(String subscriberId, Set<String> add, Set<String> remove);
    ActivityStreamsSubscription getSubscriptionByUsername(String username);
    ActivityStreamsSubscription getSubscriptionByInRoute(String inRoute);
}
