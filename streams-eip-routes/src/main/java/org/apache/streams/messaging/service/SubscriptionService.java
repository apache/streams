package org.apache.streams.messaging.service;

import org.apache.streams.osgi.components.activitysubscriber.ActivityStreamsSubscription;

import java.util.List;

public interface SubscriptionService {

    List<String> getFilters(String authToken);
    void saveFilters(ActivityStreamsSubscription subscription);
}
