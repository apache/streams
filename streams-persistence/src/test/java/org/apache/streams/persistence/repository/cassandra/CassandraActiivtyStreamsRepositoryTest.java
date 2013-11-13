package org.apache.streams.persistence.repository.cassandra;

import org.apache.streams.persistence.configuration.CassandraConfiguration;
import org.apache.streams.persistence.model.ActivityStreamsEntry;
import org.apache.streams.persistence.model.ActivityStreamsObject;
import org.apache.streams.persistence.model.cassandra.CassandraActivityStreamsEntry;
import org.apache.streams.persistence.model.cassandra.CassandraActivityStreamsObject;
import org.junit.*;

import java.io.IOException;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;

public class CassandraActiivtyStreamsRepositoryTest {
    private CassandraActivityStreamsRepository repository;

    @Before
    public void setup() throws Exception {
        CassandraConfiguration configuration = new CassandraConfiguration();
        configuration.setCassandraHost("127.0.0.1");
        configuration.setCassandraPort(9042);
        configuration.setActivitystreamsColumnFamilyName("acitivites_Test8");
        configuration.setKeyspaceName("keyspacetest");
        CassandraKeyspace keyspace = new CassandraKeyspace(configuration);

        repository = new CassandraActivityStreamsRepository(keyspace,configuration);
    }

    @After
    public void takedown(){
    }

    @Ignore
    @Test
    public void testNullTags(){
       repository.getActivitiesForProviders(null,new Date(0));
    }

    @Ignore
    @Test
    public void getActivitiesForTagsTest(){
        List<ActivityStreamsEntry> activities = repository.getActivitiesForProviders(new HashSet<String>(Arrays.asList("provider_displayname")),new Date(0));
    }

    @Ignore
    @Test
    public void saveActivity(){
        ActivityStreamsEntry entry = new CassandraActivityStreamsEntry();
        ActivityStreamsObject actor = new CassandraActivityStreamsObject();
        ActivityStreamsObject target = new CassandraActivityStreamsObject();
        ActivityStreamsObject object = new CassandraActivityStreamsObject();
        ActivityStreamsObject provider = new CassandraActivityStreamsObject();

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
}
