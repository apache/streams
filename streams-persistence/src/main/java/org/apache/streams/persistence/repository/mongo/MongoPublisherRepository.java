package org.apache.streams.persistence.repository.mongo;

import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.QueryBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.streams.persistence.configuration.MongoConfiguration;
import org.apache.streams.persistence.model.ActivityStreamsPublisher;
import org.apache.streams.persistence.model.mongo.MongoPublisher;
import org.apache.streams.persistence.repository.PublisherRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class MongoPublisherRepository implements PublisherRepository {
    private static final Log log = LogFactory.getLog(MongoPublisherRepository.class);

    private DBCollection publisherCollection;

    @Autowired
    public MongoPublisherRepository(MongoDatabase database, MongoConfiguration configuration) {
        this.publisherCollection = database.getDb().getCollection(configuration.getPublisherCollectionName());
        publisherCollection.setObjectClass(MongoPublisher.class);
    }

    @Override
    public ActivityStreamsPublisher getPublisherByInRoute(String inRoute) {
        DBObject query = QueryBuilder.start("inRoute").is(inRoute).get();
        return (ActivityStreamsPublisher) publisherCollection.findOne(query);
    }

    @Override
    public ActivityStreamsPublisher getPublisherBySrc(String src) {
        DBObject query = QueryBuilder.start("src").is(src).get();
        return (ActivityStreamsPublisher) publisherCollection.findOne(query);
    }

    @Override
    public void save(ActivityStreamsPublisher publisher) {
        if (publisher instanceof DBObject) {
            publisherCollection.save((DBObject)publisher);
        }
    }
}
