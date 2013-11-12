package org.apache.streams.persistence.repository.cassandra;

import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.exceptions.AlreadyExistsException;
import com.datastax.driver.core.querybuilder.Insert;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import com.datastax.driver.core.querybuilder.Select;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.streams.persistence.configuration.CassandraConfiguration;
import org.apache.streams.persistence.model.ActivityStreamsEntry;
import org.apache.streams.persistence.model.ActivityStreamsObject;
import org.apache.streams.persistence.model.cassandra.CassandraActivityStreamsEntry;
import org.apache.streams.persistence.model.cassandra.CassandraActivityStreamsObject;
import org.apache.streams.persistence.repository.ActivityStreamsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class CassandraActivityStreamsRepository implements ActivityStreamsRepository {

    private static final Log LOG = LogFactory.getLog(CassandraActivityStreamsRepository.class);

    private CassandraKeyspace keyspace;
    private CassandraConfiguration configuration;

    @Autowired
    public CassandraActivityStreamsRepository(CassandraKeyspace keyspace, CassandraConfiguration configuration) {
        this.configuration = configuration;
        this.keyspace = keyspace;

        try {
            String createTableCql = "CREATE TABLE " + configuration.getActivitystreamsColumnFamilyName() + " (" +
                    "id text, " +
                    "published timestamp, " +
                    "verb text, " +

                    "actor_displayname text, " +
                    "actor_id text, " +
                    "actor_url text, " +
                    "actor_objecttype text, " +

                    "target_displayname text, " +
                    "target_id text, " +
                    "target_url text, " +

                    "provider_url text, " +
                    "provider_displayname text, " +

                    "object_url text, " +
                    "object_displayname text, " +
                    "object_id text, " +
                    "object_objecttype text, " +

                    "PRIMARY KEY (provider_displayname, published));";

            keyspace.getSession().execute(createTableCql);
        } catch (AlreadyExistsException ignored) {
        }
    }

    @Override
    public void save(ActivityStreamsEntry entry) {
        Insert query = QueryBuilder.insertInto(configuration.getActivitystreamsColumnFamilyName())
                .value("id", entry.getId())
                .value("published", entry.getPublished().getTime())
                .value("verb", entry.getVerb())

                .value("actor_displayname", entry.getActor().getDisplayName())
                .value("actor_objecttype", entry.getActor().getObjectType())
                .value("actor_id", entry.getActor().getId())
                .value("actor_url", entry.getActor().getUrl())

                .value("target_displayname", entry.getTarget().getDisplayName())
                .value("target_id", entry.getTarget().getId())
                .value("target_url", entry.getTarget().getUrl())

                .value("provider_url", entry.getProvider().getUrl())
                .value("provider_displayname", entry.getProvider().getDisplayName())

                .value("object_displayname", entry.getObject().getDisplayName())
                .value("object_objecttype", entry.getObject().getObjectType())
                .value("object_id", entry.getObject().getId())
                .value("object_url", entry.getObject().getUrl());

        keyspace.getSession().execute(query);
    }

    @Override
    public List<ActivityStreamsEntry> getActivitiesForProviders(Set<String> providers, Date lastUpdated) {
        List<ActivityStreamsEntry> results = new ArrayList<ActivityStreamsEntry>();

        for (String providerName : providers) {
            Select query = QueryBuilder.select().from(configuration.getActivitystreamsColumnFamilyName())
                    .where(QueryBuilder.eq("provider_displayname", providerName))
                    .and(QueryBuilder.gt("published", lastUpdated))
                    .orderBy(QueryBuilder.desc("published")).limit(100).allowFiltering();
            ResultSet set = keyspace.getSession().execute(query);

            //iterate through the results and create a new ActivityStreamsEntry for every result returned

            for (Row row : set) {
                ActivityStreamsEntry entry = new CassandraActivityStreamsEntry();
                ActivityStreamsObject actor = new CassandraActivityStreamsObject();
                ActivityStreamsObject target = new CassandraActivityStreamsObject();
                ActivityStreamsObject object = new CassandraActivityStreamsObject();
                ActivityStreamsObject provider = new CassandraActivityStreamsObject();

                actor.setDisplayName(row.getString("actor_displayname"));
                actor.setId(row.getString("actor_id"));
                actor.setObjectType(row.getString("actor_objecttype"));
                actor.setUrl(row.getString("actor_url"));

                target.setDisplayName(row.getString("target_displayname"));
                target.setId(row.getString("target_id"));
                target.setUrl(row.getString("target_url"));

                object.setDisplayName(row.getString("object_displayname"));
                object.setObjectType(row.getString("object_objecttype"));
                object.setUrl(row.getString("object_url"));
                object.setId(row.getString("object_id"));

                provider.setUrl(row.getString("provider_url"));
                provider.setUrl(row.getString("provider_displayname"));

                entry.setPublished(row.getDate("published"));
                entry.setVerb(row.getString("verb"));
                entry.setId(row.getString("id"));
                entry.setActor(actor);
                entry.setTarget(target);
                entry.setObject(object);
                entry.setProvider(provider);

                results.add(entry);
            }
        }

        return results;
    }

    public void dropTable(String table) {
        String cql = "DROP TABLE " + table;
        keyspace.getSession().execute(cql);
    }

}
