package org.apache.streams.cassandra.repository.impl;

import com.netflix.astyanax.connectionpool.OperationResult;
import com.netflix.astyanax.model.CqlResult;
import org.apache.streams.cassandra.model.CassandraActivityStreamsEntry;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

public class CassandraActivityStreamsRepositoryTest {

    public CassandraActivityStreamsRepository repository;


    @Before
    public void setup() {
        repository = new CassandraActivityStreamsRepository();
    }

    @Test
    public void getActivitiesForQuery() {
        String cql = "select * from Activities";
        List<CassandraActivityStreamsEntry> list= repository.getActivitiesForQuery(cql);
        assert(list != null);
    }
}
