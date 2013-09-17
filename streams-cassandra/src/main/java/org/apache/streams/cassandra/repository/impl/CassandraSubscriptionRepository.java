package org.apache.streams.cassandra.repository.impl;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.exceptions.AlreadyExistsException;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.streams.osgi.components.activitysubscriber.ActivityStreamsSubscription;

public class CassandraSubscriptionRepository {
    private final String KEYSPACE_NAME = "keytest";
    private final String TABLE_NAME = "subtest";

    private static final Log LOG = LogFactory.getLog(CassandraActivityStreamsRepository.class);

    private Cluster cluster;
    private Session session;

    public CassandraSubscriptionRepository() {
        cluster = Cluster.builder().addContactPoint("127.0.0.1").build();
        session = cluster.connect();

        try {
            session.execute("CREATE KEYSPACE " + KEYSPACE_NAME + " WITH replication = { 'class': 'SimpleStrategy','replication_factor' : 1 };");
        } catch (AlreadyExistsException ignored) {
        }
        //connect to the keyspace
        session = cluster.connect(KEYSPACE_NAME);
        try {
            session.execute("CREATE TABLE " + TABLE_NAME + " (" +
                    "id text, " +
                    "filters text, " +

                    "PRIMARY KEY (id));");
        } catch (AlreadyExistsException ignored) {
        }
    }

    public String getFilters(String id){
        String cql = "SELECT * FROM " + TABLE_NAME + " WHERE id = '" + id+"';";

        ResultSet set = session.execute(cql);

        return set.one().getString("filters");
    }

    public void save(ActivityStreamsSubscription subscription){
        String cql = "INSERT INTO " + TABLE_NAME + " (" +
                "id, filters) " +
                "VALUES ('" +
                subscription.getAuthToken() + "','" +
                StringUtils.join(subscription.getFilters(), " ") +

                "')";
        session.execute(cql);
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
