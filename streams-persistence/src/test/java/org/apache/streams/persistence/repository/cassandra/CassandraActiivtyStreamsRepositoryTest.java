package org.apache.streams.persistence.repository.cassandra;

import org.apache.streams.persistence.configuration.CassandraConfiguration;
import org.apache.streams.persistence.model.ActivityStreamsEntry;
import org.apache.streams.persistence.model.ActivityStreamsObject;
import org.apache.streams.persistence.model.cassandra.CassandraActivityStreamsEntry;
import org.apache.streams.persistence.model.cassandra.CassandraActivityStreamsObject;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;

public class CassandraActiivtyStreamsRepositoryTest {
    private CassandraActivityStreamsRepository repository;

    @Before
    public void setup(){
        CassandraConfiguration configuration = new CassandraConfiguration();
        configuration.setCassandraPort("127.0.0.1");
        configuration.setActivitystreamsColumnFamilyName("acitivites_Test34");
        configuration.setKeyspaceName("keyspacetest");
        CassandraKeyspace keyspace = new CassandraKeyspace(configuration);

        repository = new CassandraActivityStreamsRepository(keyspace,configuration);
    }

    @Ignore
    @Test
    public void testNullTags(){
       repository.getActivitiesForTags(null,new Date(0));
    }

    @Ignore
    @Test
    public void getActivitiesForTagsTest(){
        repository.getActivitiesForTags(new HashSet<String>(Arrays.asList("tags")),new Date(0));
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

        entry.setPublished(new Date());
        entry.setVerb("verb");
        entry.setId("id");
        entry.setTags("tags");
        entry.setActor(actor);
        entry.setTarget(target);
        entry.setObject(object);
        entry.setProvider(provider);

        repository.save(entry);
    }
}
