package org.apache.streams.persistence.repository.cassandra;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.exceptions.AlreadyExistsException;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.rave.model.ActivityStreamsEntry;
import org.apache.rave.model.ActivityStreamsObject;
import org.apache.rave.portal.model.impl.ActivityStreamsEntryImpl;
import org.apache.rave.portal.model.impl.ActivityStreamsObjectImpl;
import org.apache.streams.persistence.configuration.CassandraConfiguration;
import org.apache.streams.persistence.model.cassandra.CassandraActivityStreamsEntry;
import org.apache.streams.persistence.repository.ActivityStreamsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Component
public class CassandraActivityStreamsRepository implements ActivityStreamsRepository{

    private static final Log LOG = LogFactory.getLog(CassandraActivityStreamsRepository.class);

    private CassandraKeyspace keyspace;
    private CassandraConfiguration configuration;

    @Autowired
    public CassandraActivityStreamsRepository(CassandraKeyspace keyspace, CassandraConfiguration configuration) {
        this.configuration = configuration;
        this.keyspace = keyspace;

        try {
            keyspace.getSession().execute("CREATE TABLE " + configuration.getActivitystreamsColumnFamilyName() + " (" +
                    "id text, " +
                    "published timestamp, " +
                    "verb text, " +
                    "tags text, " +

                    "actor_displayname text, " +
                    "actor_id text, " +
                    "actor_url text, " +
                    "actor_objecttype text, " +

                    "target_displayname text, " +
                    "target_id text, " +
                    "target_url text, " +

                    "provider_url text, " +

                    "object_url text, " +
                    "object_displayname text, " +
                    "object_id text, " +
                    "object_objecttype text, " +

                    "PRIMARY KEY (id, tags, published));");
        } catch (AlreadyExistsException ignored) {
        }
    }

    public void save(ActivityStreamsEntry entry) {
        //TODO: this should be random UUID
        String sql = "INSERT INTO " + configuration.getActivitystreamsColumnFamilyName() + " (" +
                "id, published, verb, tags, " +
                "actor_displayname, actor_objecttype, actor_id, actor_url, " +
                "target_displayname, target_id, target_url, " +
                "provider_url, " +
                "object_displayname, object_objecttype, object_id, object_url) " +
                "VALUES ('" +
                entry.getId() + "','" +
                entry.getPublished().getTime() + "','" +
                entry.getVerb() + "','" +
                entry.getTags() + "','" +

                entry.getActor().getDisplayName() + "','" +
                entry.getActor().getObjectType() + "','" +
                entry.getActor().getId() + "','" +
                entry.getActor().getUrl() + "','" +

                entry.getTarget().getDisplayName() + "','" +
                entry.getTarget().getId() + "','" +
                entry.getTarget().getUrl() + "','" +

                entry.getProvider().getUrl() + "','" +

                entry.getObject().getDisplayName() + "','" +
                entry.getObject().getObjectType() + "','" +
                entry.getObject().getId() + "','" +
                entry.getObject().getUrl() +

                "')";
        keyspace.getSession().execute(sql);
    }

    public List<ActivityStreamsEntry> getActivitiesForFilters(List<String> filters, Date lastUpdated) {
        List<ActivityStreamsEntry> results = new ArrayList<ActivityStreamsEntry>();

        for (String tag : filters) {
            String cql = "SELECT * FROM " + configuration.getActivitystreamsColumnFamilyName() + " WHERE ";

            //add filters
            cql = cql + " tags = '" + tag + "' AND ";

            //specify last modified
            cql = cql + "published > " + lastUpdated.getTime() + " ALLOW FILTERING";

            //execute the cql query and store the results
            //TODO: will this ever return a null ResultSet
            ResultSet set = keyspace.getSession().execute(cql);

            //iterate through the results and create a new ActivityStreamsEntry for every result returned

            for (Row row : set) {
                ActivityStreamsEntry entry = new CassandraActivityStreamsEntry();
                ActivityStreamsObject actor = new ActivityStreamsObjectImpl();
                ActivityStreamsObject target = new ActivityStreamsObjectImpl();
                ActivityStreamsObject object = new ActivityStreamsObjectImpl();
                ActivityStreamsObject provider = new ActivityStreamsObjectImpl();

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

                entry.setPublished(row.getDate("published"));
                entry.setVerb(row.getString("verb"));
                entry.setId(row.getString("id"));
                entry.setTags(row.getString("tags"));
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
