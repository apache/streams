package org.apache.streams.cassandra.repository.impl;

import org.apache.rave.model.ActivityStreamsEntry;
import org.apache.rave.model.ActivityStreamsObject;
import org.apache.rave.portal.model.impl.ActivityStreamsEntryImpl;
import org.apache.rave.portal.model.impl.ActivityStreamsObjectImpl;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class CassandraActivityStreamsRepositoryTest {

    public CassandraActivityStreamsRepository repository;


    @Before
    public void setup() {
        repository = new CassandraActivityStreamsRepository();
    }

    @Ignore
    @Test
    public void saveActivity() {
        ActivityStreamsEntry entry = new ActivityStreamsEntryImpl();
        ActivityStreamsObject actor = new ActivityStreamsObjectImpl();
        ActivityStreamsObject target = new ActivityStreamsObjectImpl();
        ActivityStreamsObject object = new ActivityStreamsObjectImpl();

        actor.setId("actorid1");
        actor.setUrl("actorurl1");
        actor.setDisplayName("actorname1");

        target.setId("targetid1");
        target.setUrl("targeturl1");
        target.setDisplayName("r501");

        object.setId("objectid1");
        object.setDisplayName("objectname1");

        entry.setId("one");
        entry.setVerb("verb1");
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
        String cql = "'r501'";
        List<String> f = Arrays.asList(cql);
        Date d = new Date(0);
        List<ActivityStreamsEntry> results = repository.getActivitiesForFilters(f,d);
    }

    @Ignore
    @Test
    public void dropTableTest(){
        repository.dropTable("coltest");
    }
}
