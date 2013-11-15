package org.apache.streams.persistence.repository.mongo;


import org.apache.streams.persistence.configuration.MongoConfiguration;
import org.apache.streams.persistence.model.ActivityStreamsEntry;
import org.apache.streams.persistence.model.ActivityStreamsObject;
import org.apache.streams.persistence.model.mongo.MongoActivityStreamsEntry;
import org.apache.streams.persistence.model.mongo.MongoActivityStreamsObject;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;

public class MongoActivityStreamsRepositoryTest {
    private MongoActivityStreamsRepository repository;
    private MongoConfiguration configuration;
    private MongoDatabase database;

    @Before
    public void setup() {
        configuration = new MongoConfiguration();
        configuration.setDbName("testdb1");
        configuration.setActivitystreamsCollectionName("activitystreamstest2");
        database = new MongoDatabase(configuration);
        repository = new MongoActivityStreamsRepository(database, configuration);
    }

    @Ignore
    @Test
    public void saveActivity(){
        ActivityStreamsEntry entry = new MongoActivityStreamsEntry();
        ActivityStreamsObject actor = new MongoActivityStreamsObject();
        ActivityStreamsObject target = new MongoActivityStreamsObject();
        ActivityStreamsObject object = new MongoActivityStreamsObject();
        ActivityStreamsObject provider = new MongoActivityStreamsObject();

        actor.setDisplayName("actor_displayname");
        actor.setId("actor_id");
        actor.setObjectType("actor_objecttype");
        actor.setUrl("actor_url");

        target.setDisplayName("target_displayname");
        target.setId("target_id");
        target.setUrl("target_url");

        object.setDisplayName("object_displayname");
        object.setObjectType("object_objecttype");
        object.setUrl("object_url");
        object.setId("object_id");

        provider.setUrl("provider_url");
        provider.setDisplayName("provider_displayname");

        entry.setPublished(new Date());
        entry.setVerb("verb");
        entry.setId("newid");
        entry.setTags("tags");
        entry.setActor(actor);
        entry.setTarget(target);
        entry.setObject(object);
        entry.setProvider(provider);

        repository.save(entry);
    }

    @Ignore
    @Test
    public void getActivitiesForProvidersTest(){
        List<ActivityStreamsEntry> activities = repository.getActivitiesForFilters(new HashSet<String>(Arrays.asList("provider_displayname")),new Date(0));
    }

}
