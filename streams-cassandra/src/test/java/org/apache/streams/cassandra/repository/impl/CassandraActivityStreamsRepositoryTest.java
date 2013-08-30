package org.apache.streams.cassandra.repository.impl;

import org.apache.rave.model.ActivityStreamsEntry;
import org.apache.rave.model.ActivityStreamsObject;
import org.apache.rave.portal.model.impl.ActivityStreamsEntryImpl;
import org.apache.rave.portal.model.impl.ActivityStreamsObjectImpl;
import org.apache.streams.cassandra.model.CassandraActivityStreamsEntry;
import org.junit.Before;
import org.junit.Test;

import java.util.Date;
import java.util.List;

public class CassandraActivityStreamsRepositoryTest {

    public CassandraActivityStreamsRepository repository;


    @Before
    public void setup() {
        repository = new CassandraActivityStreamsRepository();
    }

    @Test
    public void saveActivity() {
        ActivityStreamsEntry entry = new ActivityStreamsEntryImpl();
        ActivityStreamsObject actor = new ActivityStreamsObjectImpl();
        ActivityStreamsObject target = new ActivityStreamsObjectImpl();
        ActivityStreamsObject object = new ActivityStreamsObjectImpl();

        actor.setId("actorid1");
        actor.setDisplayName("actorname1");

        target.setId("targetid1");
        target.setDisplayName("targetname1");

        object.setId("objectid1");
        object.setDisplayName("objectname1");

        entry.setId("one");
        entry.setVerb("verb1");
        Date d = new Date();
        entry.setPublished(d);
        entry.setActor(actor);
        entry.setObject(object);
        entry.setTarget(target);

        //repository.save(entry);
    }

    @Test
    public void getActivity() {
        String cql = "SELECT * FROM coltest WHERE published > '2010-10-10' LIMIT 1 ALLOW FILTERING";
        List<ActivityStreamsEntry> results = repository.getActivitiesForQuery(cql);
    }

    @Test
    public void dropTableTest(){
        //repository.dropTable("coltest");
    }
}
