package org.apache.streams.components.service;

import java.io.IOException;
import java.util.Date;
import java.util.List;


public interface StreamsActivityRepositoryService {

    void receiveActivity(String activityJSON) throws IOException;

    List<String> getActivitiesForFilters(List<String> filters, Date lastUpdated);
}
