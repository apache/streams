package org.apache.streams.components.service;

import org.apache.streams.persistence.model.ActivityStreamsEntry;
import org.apache.streams.persistence.model.ActivityStreamsPublisher;

import java.util.Date;
import java.util.List;
import java.util.Set;


public interface StreamsActivityRepositoryService {

    void receiveActivity(ActivityStreamsPublisher publisher, String activityJSON) throws Exception;

    List<ActivityStreamsEntry> getActivitiesForProviders(Set<String> providers, Date lastUpdated);
}
