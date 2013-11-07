package org.apache.streams.persistence.repository.cassandra;

import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.exceptions.AlreadyExistsException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.streams.persistence.configuration.CassandraConfiguration;
import org.apache.streams.persistence.model.ActivityStreamsPublisher;
import org.apache.streams.persistence.model.cassandra.CassandraPublisher;
import org.apache.streams.persistence.repository.PublisherRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class CassandraPublisherRepository implements PublisherRepository {

    private static final Log LOG = LogFactory.getLog(CassandraActivityStreamsRepository.class);

    private CassandraKeyspace keyspace;
    private CassandraConfiguration configuration;

    @Autowired
    public CassandraPublisherRepository(CassandraKeyspace keyspace, CassandraConfiguration configuration) {
        this.configuration = configuration;
        this.keyspace = keyspace;

        try {
            String createTableCQL = "CREATE TABLE " + configuration.getPublisherColumnFamilyName() + " (" +
                    "inroute text, " +
                    "src text, " +

                    "PRIMARY KEY (inroute))";
            String srcIndexCQL = "CREATE INDEX ON " + configuration.getPublisherColumnFamilyName() + "(src)";

            keyspace.getSession().execute(createTableCQL);
            keyspace.getSession().execute(srcIndexCQL);
        } catch (AlreadyExistsException ignored) {
        }
    }

    @Override
    public void save(ActivityStreamsPublisher publisher) {
        String cql = "INSERT INTO " + configuration.getPublisherColumnFamilyName() + " (" +
                "inroute, src) " +
                "VALUES ('" +
                publisher.getInRoute() + "','" +
                publisher.getSrc() +

                "')";
        keyspace.getSession().execute(cql);
    }

    @Override
    public ActivityStreamsPublisher getPublisherByInRoute(String inRoute) {
        String cql = "SELECT * FROM " + configuration.getPublisherColumnFamilyName() + " WHERE inroute = '" + inRoute + "'";

        ResultSet set = keyspace.getSession().execute(cql);
        Row row = set.one();

        if (row != null) {
            ActivityStreamsPublisher publisher = new CassandraPublisher();
            publisher.setSrc(row.getString("src"));
            publisher.setInRoute(row.getString("inroute"));

            return publisher;
        }

        return null;
    }

    @Override
    public ActivityStreamsPublisher getPublisherBySrc(String src) {
        String cql = "SELECT * FROM " + configuration.getPublisherColumnFamilyName() + " WHERE src = '" + src + "'";

        ResultSet set = keyspace.getSession().execute(cql);
        Row row = set.one();

        if (row != null) {
            ActivityStreamsPublisher publisher = new CassandraPublisher();
            publisher.setSrc(row.getString("src"));
            publisher.setInRoute(row.getString("inroute"));

            return publisher;
        }

        return null;
    }

    public void dropTable() {
        String cql = "TRUNCATE " + configuration.getPublisherColumnFamilyName();
        keyspace.getSession().execute(cql);
    }
}
