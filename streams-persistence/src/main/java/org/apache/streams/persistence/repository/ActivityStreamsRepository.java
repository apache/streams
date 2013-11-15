package org.apache.streams.persistence.repository;


import org.apache.streams.persistence.model.ActivityStreamsEntry;

import java.util.Date;
import java.util.List;
import java.util.Set;

public interface ActivityStreamsRepository {
    void save(ActivityStreamsEntry entry);
    List<ActivityStreamsEntry> getActivitiesForFilters(Set<String> filters, Date lastUpdated);
    void dropTable(String table);

}
