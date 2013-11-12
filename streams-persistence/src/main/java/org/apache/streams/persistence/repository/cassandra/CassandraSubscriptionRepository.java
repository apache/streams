package org.apache.streams.persistence.repository.cassandra;

import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.exceptions.AlreadyExistsException;
import com.datastax.driver.core.querybuilder.Insert;
import com.datastax.driver.core.querybuilder.QueryBuilder;

import com.datastax.driver.core.querybuilder.Select;
import com.datastax.driver.core.querybuilder.Update;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.streams.persistence.configuration.CassandraConfiguration;
import org.apache.streams.persistence.model.ActivityStreamsSubscription;
import org.apache.streams.persistence.model.cassandra.CassandraSubscription;
import org.apache.streams.persistence.repository.SubscriptionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashSet;
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
                    "filters set<text>, " +

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

        Select.Where query = QueryBuilder.select().from(configuration.getSubscriptionColumnFamilyName()).where(QueryBuilder.eq("inroute",inRoute));
        ResultSet set = keyspace.getSession().execute(query);
        Row row = set.one();

        if (row != null) {
            ActivityStreamsSubscription subscription = new CassandraSubscription();

            subscription.setInRoute(row.getString("inroute"));
            subscription.setUsername(row.getString("username"));
            subscription.setFilters(row.getSet("filters", String.class));

            return subscription;
        }
        return null;
    }

    @Override
    public ActivityStreamsSubscription getSubscriptionByUsername(String username) {
        LOG.info("retreiving subscription for username: " + username);

        Select.Where query = QueryBuilder.select().from(configuration.getSubscriptionColumnFamilyName()).where(QueryBuilder.eq("username",username));
        ResultSet set = keyspace.getSession().execute(query);
        Row row = set.one();

        if (row != null) {
            ActivityStreamsSubscription subscription = new CassandraSubscription();

            subscription.setInRoute(row.getString("inroute"));
            subscription.setUsername(row.getString("username"));
            subscription.setFilters(row.getSet("filters", String.class));

            return subscription;
        }
        return null;
    }

    @Override
    public List<ActivityStreamsSubscription> getAllSubscriptions() {
        LOG.info("getting all subscriptions");

        Select query = QueryBuilder.select().all().from(configuration.getSubscriptionColumnFamilyName());

        ResultSet set = keyspace.getSession().execute(query);
        List<ActivityStreamsSubscription> results = new ArrayList<ActivityStreamsSubscription>();

        for (Row row : set) {
            ActivityStreamsSubscription subscription = new CassandraSubscription();

            subscription.setInRoute(row.getString("inroute"));
            subscription.setUsername(row.getString("username"));
            subscription.setFilters(row.getSet("filters", String.class));

            results.add(subscription);
        }

        return results;
    }

    @Override
    public void save(ActivityStreamsSubscription subscription) {
        LOG.info("saving subscription username: " + subscription.getUsername() + " inRoute " + subscription.getInRoute());

        //TODO: will this overwrite?
        Insert query = QueryBuilder.insertInto(configuration.getSubscriptionColumnFamilyName())
                .value("inroute", subscription.getInRoute())
                .value("username", subscription.getUsername())
                .value("filters", new HashSet<String>());
        keyspace.getSession().execute(query);
    }

    @Override
    public void updateFilters(String subscriberId, Set<String> add, Set<String> remove) {
        LOG.info("updating filters inRoute: " + subscriberId + " add: " + add + " remove: " + remove);

        Update.Where query = QueryBuilder.update(configuration.getSubscriptionColumnFamilyName())
                .with(QueryBuilder.addAll("filters", add))
                .and(QueryBuilder.removeAll("filters", remove))
                .where(QueryBuilder.eq("inroute", subscriberId));
        keyspace.getSession().execute(query);
    }
}
