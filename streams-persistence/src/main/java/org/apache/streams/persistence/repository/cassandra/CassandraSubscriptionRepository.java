package org.apache.streams.persistence.repository.cassandra;

import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.exceptions.AlreadyExistsException;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.streams.persistence.configuration.CassandraConfiguration;
import org.apache.streams.persistence.model.ActivityStreamsSubscription;
import org.apache.streams.persistence.model.cassandra.CassandraSubscription;
import org.apache.streams.persistence.repository.SubscriptionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Component
public class CassandraSubscriptionRepository implements SubscriptionRepository {
    private Log LOG = LogFactory.getLog(CassandraSubscriptionRepository.class);

    private CassandraKeyspace keyspace;
    private CassandraConfiguration configuration;

    @Autowired
    public CassandraSubscriptionRepository(CassandraKeyspace keyspace, CassandraConfiguration configuration) {
        LOG.info("connecting subscription repository");

        this.keyspace = keyspace;
        this.configuration = configuration;

        try {
            String createTableCQL = "CREATE TABLE " + configuration.getSubscriptionColumnFamilyName() + " (" +
                    "inroute text, " +
                    "username text, " +
                    "tags set<text>, " +

                    "PRIMARY KEY (inroute));";
            String usernameIndexCQL = "CREATE INDEX ON " + configuration.getSubscriptionColumnFamilyName() + "(username)";

            keyspace.getSession().execute(createTableCQL);
            keyspace.getSession().execute(usernameIndexCQL);

        } catch (AlreadyExistsException ignored) {
        }
    }

    @Override
    public ActivityStreamsSubscription getSubscriptionByInRoute(String inRoute) {
        LOG.info("retreiving subscription for inRoute: " + inRoute);

        String cql = "SELECT * FROM " + configuration.getSubscriptionColumnFamilyName() + " WHERE inroute = '" + inRoute + "';";

        ResultSet set = keyspace.getSession().execute(cql);
        Row row = set.one();

        if (row != null) {
            ActivityStreamsSubscription subscription = new CassandraSubscription();

            subscription.setInRoute(row.getString("inroute"));
            subscription.setUsername(row.getString("username"));
            subscription.setTags(row.getSet("tags", String.class));

            return subscription;
        }
        return null;
    }

    @Override
    public ActivityStreamsSubscription getSubscriptionByUsername(String username) {
        LOG.info("retreiving subscription for username: " + username);

        String cql = "SELECT * FROM " + configuration.getSubscriptionColumnFamilyName() + " WHERE username = '" + username + "';";

        ResultSet set = keyspace.getSession().execute(cql);
        Row row = set.one();

        if (row != null) {
            ActivityStreamsSubscription subscription = new CassandraSubscription();

            subscription.setInRoute(row.getString("inroute"));
            subscription.setUsername(row.getString("username"));
            subscription.setTags(row.getSet("tags", String.class));

            return subscription;
        }
        return null;
    }

    @Override
    public List<ActivityStreamsSubscription> getAllSubscriptions() {
        LOG.info("getting all subscriptions");

        String cql = "SELECT * FROM " + configuration.getSubscriptionColumnFamilyName();

        ResultSet set = keyspace.getSession().execute(cql);
        List<ActivityStreamsSubscription> results = new ArrayList<ActivityStreamsSubscription>();

        for (Row row : set) {
            ActivityStreamsSubscription subscription = new CassandraSubscription();

            subscription.setInRoute(row.getString("inroute"));
            subscription.setUsername(row.getString("username"));
            subscription.setTags(row.getSet("tags", String.class));

            results.add(subscription);
        }

        return results;
    }

    @Override
    public void save(ActivityStreamsSubscription subscription) {
        LOG.info("saving subscription username: " + subscription.getUsername() + " inRoute " + subscription.getInRoute());

        //TODO: will this overwrite?
        String cql = "INSERT INTO " + configuration.getSubscriptionColumnFamilyName() + " (" +
                "inroute, username, tags) " +
                "VALUES ('" +
                subscription.getInRoute() + "','" +
                subscription.getUsername() + "'," +
                "{}" +

                ")";
        keyspace.getSession().execute(cql);
    }

    @Override
    public void updateTags(String subscriberId, Set<String> add, Set<String> remove) {
        LOG.info("updating tags inRoute: " + subscriberId + " add: " + add + " remove: " + remove);

        String cql = "UPDATE " + configuration.getSubscriptionColumnFamilyName() +
                " SET tags = tags + {'" +
                StringUtils.join(add, "','") + "'}," +
                " tags = tags - {'" +
                StringUtils.join(remove, "','") + "'}" +
                " WHERE inroute = '" + subscriberId + "'";

        keyspace.getSession().execute(cql);
    }
}
