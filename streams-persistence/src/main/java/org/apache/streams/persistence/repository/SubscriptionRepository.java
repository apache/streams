package org.apache.streams.persistence.repository;

import org.apache.streams.persistence.model.ActivityStreamsSubscription;

import java.util.List;
import java.util.Set;

public interface SubscriptionRepository {
    ActivityStreamsSubscription getSubscriptionByInRoute(String inRoute);
    ActivityStreamsSubscription getSubscriptionByUsername(String username);
    List<ActivityStreamsSubscription> getAllSubscriptions();
    void save(ActivityStreamsSubscription subscription);
    void updateFilters(String subscriberId, Set<String> add, Set<String> remove);
}
