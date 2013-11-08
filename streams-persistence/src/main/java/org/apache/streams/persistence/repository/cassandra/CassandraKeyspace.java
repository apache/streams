package org.apache.streams.persistence.repository.cassandra;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.exceptions.AlreadyExistsException;
import com.datastax.driver.core.policies.ConstantReconnectionPolicy;
import com.datastax.driver.core.policies.DowngradingConsistencyRetryPolicy;
import org.apache.streams.persistence.configuration.CassandraConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.net.InetAddress;

@Component
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
