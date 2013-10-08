package org.apache.streams.components.service;

import org.apache.streams.persistence.model.ActivityStreamsSubscription;

import java.util.List;

public interface StreamsSubscriptionRepositoryService{

    List<String> getFilters(String authToken);
    void saveFilters(ActivityStreamsSubscription subscription);
}
