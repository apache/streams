package org.apache.streams.persistence.repository.cassandra;

import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.exceptions.AlreadyExistsException;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.streams.persistence.configuration.CassandraConfiguration;
import org.apache.streams.persistence.model.ActivityStreamsSubscription;
import org.apache.streams.persistence.repository.SubscriptionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class CassandraSubscriptionRepository implements SubscriptionRepository{
    private static final Log LOG = LogFactory.getLog(CassandraSubscriptionRepository.class);

    private CassandraKeyspace keyspace;
    private CassandraConfiguration configuration;

    @Autowired
    public CassandraSubscriptionRepository(CassandraKeyspace keyspace, CassandraConfiguration configuration) {
        this.keyspace = keyspace;
        this.configuration = configuration;

        try {
            keyspace.getSession().execute("CREATE TABLE " + configuration.getSubscriptionColumnFamilyName() + " (" +
                    "id text, " +
                    "filters text, " +

                    "PRIMARY KEY (id));");
        } catch (AlreadyExistsException ignored) {
        }
    }

    public String getFilters(String id){
        String cql = "SELECT * FROM " + configuration.getSubscriptionColumnFamilyName()  + " WHERE id = '" + id+"';";

        ResultSet set = keyspace.getSession().execute(cql);

        return set.one().getString("filters");
    }

    public void save(ActivityStreamsSubscription subscription){
        //TODO: will this overwrite?
        String cql = "INSERT INTO " + configuration.getSubscriptionColumnFamilyName()  + " (" +
                "id, filters) " +
                "VALUES ('" +
                subscription.getAuthToken() + "','" +
                StringUtils.join(subscription.getFilters(), " ") +

                "')";
        keyspace.getSession().execute(cql);
    }
}
