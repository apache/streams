package org.apache.streams.components.service;

import org.apache.streams.persistence.model.ActivityStreamsPublisher;

import java.io.IOException;
import java.util.Date;
import java.util.List;


public interface StreamsActivityRepositoryService {

    void receiveActivity(ActivityStreamsPublisher publisher, String activityJSON) throws Exception;

    List<String> getActivitiesForFilters(List<String> filters, Date lastUpdated);
}
