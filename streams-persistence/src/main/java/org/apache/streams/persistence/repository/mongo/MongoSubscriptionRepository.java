package org.apache.streams.persistence.repository.mongo;

import com.mongodb.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.streams.persistence.configuration.MongoConfiguration;
import org.apache.streams.persistence.model.ActivityStreamsSubscription;
import org.apache.streams.persistence.model.mongo.MongoSubscription;
import org.apache.streams.persistence.repository.SubscriptionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

@Component
public class MongoSubscriptionRepository implements SubscriptionRepository{
    private static final Log log = LogFactory.getLog(MongoActivityStreamsRepository.class);

    private DBCollection subscriptionCollection;

    @Autowired
    public MongoSubscriptionRepository(MongoDatabase database, MongoConfiguration configuration) {
        this.subscriptionCollection = database.getDb().getCollection(configuration.getSubscriptionCollectionName());
        subscriptionCollection.setObjectClass(MongoSubscription.class);
    }

    @Override
    public ActivityStreamsSubscription getSubscriptionByInRoute(String inRoute) {
        DBObject query = QueryBuilder.start("inRoute").is(inRoute).get();
        return (ActivityStreamsSubscription)subscriptionCollection.findOne(query);
    }

    @Override
    public ActivityStreamsSubscription getSubscriptionByUsername(String username) {
        DBObject query = QueryBuilder.start("username").is(username).get();
        return (ActivityStreamsSubscription)subscriptionCollection.findOne(query);
    }

    @Override
    public List<ActivityStreamsSubscription> getAllSubscriptions() {
        return (List<ActivityStreamsSubscription>)(List<?>)subscriptionCollection.find().toArray();
    }

    @Override
    public void save(ActivityStreamsSubscription subscription) {
        if (subscription instanceof DBObject) {
            subscriptionCollection.save((DBObject)subscription);
        }
    }

    @Override
    public void updateFilters(String subscriberId, Set<String> add, Set<String> remove) {
        DBObject query = QueryBuilder.start("inRoute").is(subscriberId).get();
        BasicDBObject dbAdd = new BasicDBObject().append("$addToSet",new BasicDBObject().append("filters", new BasicDBObject().append("$each", add)));
        BasicDBObject dbRemove = new BasicDBObject().append("$pullAll",new BasicDBObject().append("filters", remove));
        subscriptionCollection.update(query, dbAdd);
        subscriptionCollection.update(query, dbRemove);
    }
}
