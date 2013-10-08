package org.apache.streams.persistence.repository;

import org.apache.rave.model.ActivityStreamsEntry;

import java.util.Date;
import java.util.List;

public interface ActivityStreamsRepository {
    void save(ActivityStreamsEntry entry);
    List<ActivityStreamsEntry> getActivitiesForFilters(List<String> filters, Date lastUpdated);
    void dropTable(String table);

}
