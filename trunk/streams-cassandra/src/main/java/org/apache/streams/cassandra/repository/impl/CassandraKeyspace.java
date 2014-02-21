package org.apache.streams.cassandra.repository.impl;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.exceptions.AlreadyExistsException;
import org.apache.streams.cassandra.configuration.CassandraConfiguration;
import org.springframework.beans.factory.annotation.Autowired;

public class CassandraKeyspace {
    private CassandraConfiguration configuration;
    private Cluster cluster;
    private Session session;

    @Autowired
    public CassandraKeyspace(CassandraConfiguration configuration){
        this.configuration = configuration;

        cluster = Cluster.builder().addContactPoint(configuration.getCassandraPort()).build();
        session = cluster.connect();

        //TODO: cassandra 2 will have support for CREATE KEYSPACE IF NOT EXISTS
        try {
            session.execute("CREATE KEYSPACE " + configuration.getKeyspaceName() + " WITH replication = { 'class': 'SimpleStrategy','replication_factor' : 1 };");
        } catch (AlreadyExistsException ignored) {
        }

        //connect to the keyspace
        session = cluster.connect(configuration.getKeyspaceName());
    }

    public Session getSession(){
        return session;
    }

    @Override
    protected void finalize() throws Throwable {
        try {
            cluster.shutdown();
        } finally {
            super.finalize();
        }
    }
}
