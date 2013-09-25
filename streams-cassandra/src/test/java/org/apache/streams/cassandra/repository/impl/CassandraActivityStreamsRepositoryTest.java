package org.apache.streams.cassandra.repository.impl;

import com.datastax.driver.core.ResultSet;
import org.apache.rave.model.ActivityStreamsEntry;
import org.apache.rave.model.ActivityStreamsObject;
import org.apache.rave.portal.model.impl.ActivityStreamsEntryImpl;
import org.apache.rave.portal.model.impl.ActivityStreamsObjectImpl;
import org.apache.streams.cassandra.configuration.CassandraConfiguration;
import org.apache.streams.cassandra.model.CassandraActivityStreamsEntry;
import static org.easymock.EasyMock.*;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class CassandraActivityStreamsRepositoryTest {

    private CassandraActivityStreamsRepository repository;


    @Before
    public void setup() {
        CassandraKeyspace keyspace = createMock(CassandraKeyspace.class);
        CassandraConfiguration configuration = createMock(CassandraConfiguration.class);
        repository = new CassandraActivityStreamsRepository(keyspace, configuration);
    }

    @Ignore
    @Test
    public void saveActivity() {
        ActivityStreamsEntry entry = new ActivityStreamsEntryImpl();
        ActivityStreamsObject actor = new ActivityStreamsObjectImpl();
        ActivityStreamsObject target = new ActivityStreamsObjectImpl();
        ActivityStreamsObject object = new ActivityStreamsObjectImpl();
        ActivityStreamsObject provider = new ActivityStreamsObjectImpl();

        actor.setId("actorid1");
        actor.setUrl("actorurl1");
        actor.setDisplayName("actorname1");

        target.setId("targetid1");
        target.setUrl("targeturl1");
        target.setDisplayName("r501");

        provider.setUrl("providerurl");

        object.setId("objectid1");
        object.setDisplayName("objectname1");

        entry.setId("dink");
        entry.setVerb("verb1");
        entry.setTags("r501");
        entry.setProvider(provider);
        Date d = new Date();
        entry.setPublished(d);
        entry.setActor(actor);
        entry.setObject(object);
        entry.setTarget(target);

        repository.save(entry);
    }

    @Ignore
    @Test
    public void getActivity() {
        String cql = "tags";
        String other = "r501";
        List<String> f = Arrays.asList(cql, other);
        Date d = new Date(0);
        List<CassandraActivityStreamsEntry> results = repository.getActivitiesForFilters(f,d);
    }

    @Ignore
    @Test
    public void dropTableTest(){
        repository.dropTable("coltest");
    }
}
