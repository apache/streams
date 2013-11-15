package org.apache.streams.persistence.repository.mongo;


import org.apache.streams.persistence.configuration.MongoConfiguration;
import org.apache.streams.persistence.model.ActivityStreamsPublisher;
import org.apache.streams.persistence.model.cassandra.CassandraPublisher;
import org.apache.streams.persistence.model.mongo.MongoPublisher;
import org.apache.streams.persistence.repository.PublisherRepository;
import org.apache.streams.persistence.repository.SubscriptionRepository;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class MongoPublisherRepositoryTest {

    private PublisherRepository repository;
    private MongoConfiguration configuration;
    private MongoDatabase database;

    @Before
    public void setup() {
        configuration = new MongoConfiguration();
        configuration.setDbName("testdb1");
        configuration.setPublisherCollectionName("publishertest2");
        database = new MongoDatabase(configuration);
        repository = new MongoPublisherRepository(database, configuration);
    }

    @Ignore
    @Test
    public void saveTest(){
        ActivityStreamsPublisher publisher = new MongoPublisher();

        publisher.setId("newId");
        publisher.setSrc("http://www.google.comd");
        publisher.setInRoute("inRoute");

        repository.save(publisher);
    }

    @Ignore
    @Test
    public void getPublisherTest(){

        ActivityStreamsPublisher publisher = repository.getPublisherByInRoute("inRoute");

        assertEquals(publisher.getSrc(),("http://www.google.comd"));
        assertEquals(publisher.getId(),("newId"));
        assertEquals(publisher.getInRoute(),("inRoute"));
    }

}
