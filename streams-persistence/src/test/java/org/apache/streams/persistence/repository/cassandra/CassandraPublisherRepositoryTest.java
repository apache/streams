package org.apache.streams.persistence.repository.cassandra;

import org.apache.streams.persistence.configuration.CassandraConfiguration;
import org.apache.streams.persistence.model.ActivityStreamsPublisher;
import org.apache.streams.persistence.model.cassandra.CassandraPublisher;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class CassandraPublisherRepositoryTest {
    private CassandraPublisherRepository repository;

    @Before
    public void setup(){
        CassandraConfiguration configuration = new CassandraConfiguration();
        configuration.setCassandraHost("127.0.0.1");
        configuration.setCassandraPort(9042);
        configuration.setPublisherColumnFamilyName("publishersA");
        configuration.setKeyspaceName("keyspacetest");
        CassandraKeyspace keyspace = new CassandraKeyspace(configuration);

        repository = new CassandraPublisherRepository(keyspace,configuration);
    }

    @Ignore
    @Test
    public void saveTest(){
        ActivityStreamsPublisher publisher = new CassandraPublisher();

        publisher.setId("newId");
        publisher.setSrc("http://www.google.comd");
        publisher.setInRoute("inRoute");

        repository.save(publisher);
    }

    @Ignore
    @Test
    public void getPublisherTest(){

        ActivityStreamsPublisher publisher = repository.getPublisherByInRoute("inRoute");

        assertEquals(publisher.getSrc(),("http://www.google.com"));
        assertEquals(publisher.getId(),("newId"));
        assertEquals(publisher.getInRoute(),("inRoute"));
    }

    @Ignore
    @Test
    public void dropTableTest(){
        repository.dropTable();
    }
}
