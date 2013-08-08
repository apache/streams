package org.apache.streams.messaging.service.impl;

import org.junit.Before;
import org.junit.Test;

import java.util.List;

public class CassandraActivityServiceTest {

    private CassandraActivityService cassandraActivityService;

    @Before
    public void setup(){
        cassandraActivityService = new CassandraActivityService();
    }

    @Test
    public void getActivititiesForQueryTest(){
        List<String> activities = cassandraActivityService.getActivitiesForQuery("select * from Activities");
        assert(activities != null);
    }
}
