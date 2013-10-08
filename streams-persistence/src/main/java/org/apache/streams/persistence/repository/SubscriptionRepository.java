package org.apache.streams.persistence.repository;

import org.apache.streams.persistence.model.ActivityStreamsSubscription;

public interface SubscriptionRepository {
    String getFilters(String id);
    void save(ActivityStreamsSubscription subscription);
}
