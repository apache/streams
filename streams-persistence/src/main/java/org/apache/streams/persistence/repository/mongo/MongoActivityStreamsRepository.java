package org.apache.streams.persistence.repository.mongo;

import com.mongodb.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.streams.persistence.configuration.MongoConfiguration;
import org.apache.streams.persistence.model.ActivityStreamsEntry;
import org.apache.streams.persistence.model.mongo.MongoActivityStreamsEntry;
import org.apache.streams.persistence.repository.ActivityStreamsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class MongoActivityStreamsRepository implements ActivityStreamsRepository {
    private static final Log log = LogFactory.getLog(MongoActivityStreamsRepository.class);

    private MongoDatabase database;
    private MongoConfiguration configuration;
    private DBCollection activityStreamsCollection;

    @Autowired
    public MongoActivityStreamsRepository(MongoDatabase database, MongoConfiguration configuration) {
        this.database = database;
        this.activityStreamsCollection = database.getDb().getCollection(configuration.getActivitystreamsCollectionName());
        activityStreamsCollection.setObjectClass(MongoActivityStreamsEntry.class);
    }


    @Override
    public void save(ActivityStreamsEntry entry) {
        if (entry instanceof DBObject) {
            activityStreamsCollection.save((DBObject) entry);
        }
    }

    @Override
    public List<ActivityStreamsEntry> getActivitiesForFilters(Set<String> filters, Date lastUpdated) {
        List<String> filterList = new ArrayList<String>(filters);
        DBObject query = QueryBuilder.start("published").greaterThan(lastUpdated).and(QueryBuilder.start().or(
                QueryBuilder.start("provider.displayName").in(filterList).get(),
                QueryBuilder.start("actor.displayName").in(filterList).get(),
                QueryBuilder.start("object.displayName").in(filterList).get(),
                QueryBuilder.start("target.displayName").in(filterList).get(),
                QueryBuilder.start("verb").in(filterList).get()
        ).get()).get();

        DBCursor cursor = activityStreamsCollection.find(query);

        List<ActivityStreamsEntry> results = new ArrayList<ActivityStreamsEntry>();

        while (cursor.hasNext()) {
            ActivityStreamsEntry entry = (MongoActivityStreamsEntry) cursor.next();
            results.add(entry);
        }

        return results;
    }
}
