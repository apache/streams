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
import java.util.Arrays;
import java.util.List;

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
                    "inroute text, " +
                    "filters text, " +

                    "PRIMARY KEY (inroute));");
        } catch (AlreadyExistsException ignored) {
        }
    }

    public ActivityStreamsSubscription getSubscription(String inRoute){
        String cql = "SELECT * FROM " + configuration.getSubscriptionColumnFamilyName()  + " WHERE inroute = '" + inRoute+"';";

        ResultSet set = keyspace.getSession().execute(cql);

        ActivityStreamsSubscription subscription = new CassandraSubscription();

        subscription.setInRoute(set.one().getString("inroute"));
        subscription.setFilters(Arrays.asList(set.one().getString("filters").split(",")));

        return subscription;
    }

    public List<ActivityStreamsSubscription> getAllSubscriptions(){
        String cql = "SELECT * FROM " + configuration.getSubscriptionColumnFamilyName();

        ResultSet set = keyspace.getSession().execute(cql);
        List<ActivityStreamsSubscription> results = new ArrayList<ActivityStreamsSubscription>();

        for (Row row : set) {
            ActivityStreamsSubscription subscription = new CassandraSubscription();

            subscription.setInRoute(row.getString("inroute"));
            subscription.setFilters(Arrays.asList(row.getString("filters").split(",")));

            results.add(subscription);
        }

        return results;
    }

    public void save(ActivityStreamsSubscription subscription){
        //TODO: will this overwrite?
        String cql = "INSERT INTO " + configuration.getSubscriptionColumnFamilyName()  + " (" +
                "inroute, filters) " +
                "VALUES ('" +
                subscription.getInRoute() + "','" +
                StringUtils.join(subscription.getFilters(), ",") +

                "')";
        keyspace.getSession().execute(cql);
    }
}
