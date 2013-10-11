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
public class CassandraPublisherRepository implements PublisherRepository{

    private static final Log LOG = LogFactory.getLog(CassandraActivityStreamsRepository.class);

    private CassandraKeyspace keyspace;
    private CassandraConfiguration configuration;

    @Autowired
    public CassandraPublisherRepository(CassandraKeyspace keyspace, CassandraConfiguration configuration) {
        this.configuration = configuration;
        this.keyspace = keyspace;

        try {
            keyspace.getSession().execute("CREATE TABLE " + configuration.getPublisherColumnFamilyName() + " (" +
                    "id text, " +
                    "inroute text, " +
                    "src text, " +

                    "PRIMARY KEY (id, inroute));");
        } catch (AlreadyExistsException ignored) {
        }
    }

    @Override
    public void save(ActivityStreamsPublisher publisher){
        String cql = "INSERT INTO " + configuration.getPublisherColumnFamilyName()  + " (" +
                "id, inroute, src) " +
                "VALUES ('" +
                publisher.getId() + "','" +
                publisher.getInRoute() + "','" +
                publisher.getSrc() +

                "')";
        keyspace.getSession().execute(cql);
    }

    @Override
    public ActivityStreamsPublisher getPublisher(String inRoute){
        String cql = "SELECT * FROM " + configuration.getPublisherColumnFamilyName()  + " WHERE inroute = '" + inRoute+"' ALLOW FILTERING;";

        ResultSet set = keyspace.getSession().execute(cql);
        Row row = set.one();

        ActivityStreamsPublisher publisher = new CassandraPublisher();
        publisher.setId(row.getString("id"));
        publisher.setSrc(row.getString("src"));
        publisher.setInRoute(row.getString("inroute"));

        return publisher;
    }

    public void dropTable() {
        String cql = "TRUNCATE " + configuration.getPublisherColumnFamilyName();
        keyspace.getSession().execute(cql);
    }
}
