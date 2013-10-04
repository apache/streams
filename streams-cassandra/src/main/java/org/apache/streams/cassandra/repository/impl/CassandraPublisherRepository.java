package org.apache.streams.cassandra.repository.impl;

import com.datastax.driver.core.exceptions.AlreadyExistsException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.streams.cassandra.configuration.CassandraConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class CassandraPublisherRepository {

    private static final Log LOG = LogFactory.getLog(CassandraActivityStreamsRepository.class);

    private CassandraKeyspace keyspace;
    private CassandraConfiguration configuration;

    @Autowired
    public CassandraPublisherRepository(CassandraKeyspace keyspace, CassandraConfiguration configuration) {
        this.configuration = configuration;
        this.keyspace = keyspace;

//        try {
//            keyspace.getSession().execute("CREATE TABLE " + configuration.getActivitystreamsColumnFamilyName() + " (" +
//                    "id text, " +
//                    "published timestamp, " +
//
//                    "PRIMARY KEY (id, tags, published));");
//        } catch (AlreadyExistsException ignored) {
//        }
    }
}
