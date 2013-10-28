package org.apache.streams.persistence.repository;

import org.apache.streams.persistence.model.ActivityStreamsSubscription;

import java.util.List;

public interface SubscriptionRepository {
    String getSubscriptionForId(String id);
    List<ActivityStreamsSubscription> getAllSubscriptions();
    void save(ActivityStreamsSubscription subscription);
}
