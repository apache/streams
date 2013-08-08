package org.apache.streams.cassandra.repository.impl;

import com.google.common.collect.ImmutableMap;
import com.netflix.astyanax.AstyanaxContext;
import com.netflix.astyanax.Keyspace;
import com.netflix.astyanax.connectionpool.NodeDiscoveryType;
import com.netflix.astyanax.connectionpool.exceptions.BadRequestException;
import com.netflix.astyanax.connectionpool.exceptions.ConnectionException;
import com.netflix.astyanax.connectionpool.impl.ConnectionPoolConfigurationImpl;
import com.netflix.astyanax.connectionpool.impl.CountingConnectionPoolMonitor;
import com.netflix.astyanax.entitystore.DefaultEntityManager;
import com.netflix.astyanax.entitystore.EntityManager;
import com.netflix.astyanax.impl.AstyanaxConfigurationImpl;
import com.netflix.astyanax.model.ColumnFamily;
import com.netflix.astyanax.serializers.StringSerializer;
import com.netflix.astyanax.thrift.ThriftFamilyFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.streams.cassandra.model.CassandraActivityStreamsEntry;

import java.util.List;

public class CassandraActivityStreamsRepository {

    private final String KEYSPACE_NAME = "ActivityStreams";
    private final String CLUSTER_NAME = "Cluster";
    private final String COLUMN_FAMILY_NAME = "Activities";

    private static final Log LOG = LogFactory.getLog(CassandraActivityStreamsRepository.class);

    private Keyspace keyspace;
    private AstyanaxContext<Keyspace> context;
    private ColumnFamily<String, String> columnFamily;
    private EntityManager<CassandraActivityStreamsEntry, String> entityManager;

    public CassandraActivityStreamsRepository() {

        //Cassandra Context Initialization
        context = getContext();

        //start the context
        context.start();
        keyspace = context.getClient();
        columnFamily = ColumnFamily.newColumnFamily(COLUMN_FAMILY_NAME, StringSerializer.get(), StringSerializer.get());

        //create the keyspace and column if they don't exist
        try {
            createKeyspace();
            createColumn();
        } catch (ConnectionException e) {
            LOG.error("An error occured while trying to connect to the database", e);
        }

        //initialize entitymanager resposible for translating Entry objects into database ready entries
        entityManager = new DefaultEntityManager.Builder<CassandraActivityStreamsEntry, String>()
                .withEntityType(CassandraActivityStreamsEntry.class)
                .withKeyspace(keyspace)
                .withColumnFamily(columnFamily)
                .build();
    }

    //creates the context object
    private AstyanaxContext<Keyspace> getContext() {
        if (context != null) {
            return context;
        } else {
            return new AstyanaxContext.Builder()
                    .forCluster(CLUSTER_NAME)
                    .forKeyspace(KEYSPACE_NAME)
                    .withAstyanaxConfiguration(new AstyanaxConfigurationImpl()
                            .setDiscoveryType(NodeDiscoveryType.RING_DESCRIBE)
                    )
                    .withConnectionPoolConfiguration(new ConnectionPoolConfigurationImpl("ActivityStreamsConnectionPool")
                            .setPort(9160)
                            .setMaxConnsPerHost(1)
                            .setSeeds("127.0.0.1:9160")
                    )
                    .withConnectionPoolMonitor(new CountingConnectionPoolMonitor())
                    .buildKeyspace(ThriftFamilyFactory.getInstance());
        }
    }

    //creates the ActivityStreams Keyspace if it doesn't exist already
    private void createKeyspace() throws ConnectionException {
        //try describing the Keyspace, a BadRequestException will be thrown if the keyspace does not exist
        try {
            keyspace.describeKeyspace();
        } catch (BadRequestException e1) {
            //the keyspace does not exist
            //create the keyspace
            keyspace.createKeyspace(ImmutableMap.<String, Object>builder()
                    .put("strategy_options", ImmutableMap.<String, Object>builder()
                            .put("replication_factor", "1")
                            .build())
                    .put("strategy_class", "SimpleStrategy")
                    .build()
            );
        }
    }

    //creates the Activities column if it doesn't exist already
    private void createColumn() throws ConnectionException {
        if (keyspace.describeKeyspace().getColumnFamily(COLUMN_FAMILY_NAME) == null) {
            //the column does not exist
            keyspace.createColumnFamily(columnFamily, ImmutableMap.<String, Object>builder()
                    .put("default_validation_class", "UTF8Type")
                    .put("key_validation_class", "UTF8Type")
                    .put("comparator_type", "UTF8Type")
                    .build());
        }
    }

    public void save(CassandraActivityStreamsEntry entry) {
        try {
            // Inserting data
            entityManager.put(entry);
            LOG.info("Insertion of the entry with the id, " + entry.getId() + ", was a success");
        } catch (Exception e) {
            LOG.info("An error occured while inserting the entry with id, " + entry.getId(), e);
        }
    }

    public List<CassandraActivityStreamsEntry> getActivitiesForQuery(String query) {
            //return entities that match the given cql query
            LOG.info("executing the query: "+query);
            return entityManager.find(query);
    }

}
