package org.apache.streams.components.service;

import org.apache.streams.components.activitysubscriber.ActivityStreamsSubscriberWarehouse;
import org.apache.streams.components.service.impl.CassandraSubscriptionService;
import org.apache.streams.components.service.impl.StreamsSubscriberRegistrationServiceImpl;
import org.apache.streams.persistence.configuration.CassandraConfiguration;
import org.apache.streams.persistence.repository.SubscriptionRepository;
import org.apache.streams.persistence.repository.cassandra.CassandraKeyspace;
import org.apache.streams.persistence.repository.cassandra.CassandraSubscriptionRepository;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import static org.easymock.EasyMock.createMock;

public class StreamsSubscriberRegistrationServiceTest {
   private StreamsSubscriberRegistrationService streamsSubscriberRegistrationService;

    @Before
    public void setup(){

        CassandraConfiguration configuration = new CassandraConfiguration();
        configuration.setCassandraPort("127.0.0.1");
        configuration.setSubscriptionColumnFamilyName("subscriptionstestB");
        configuration.setKeyspaceName("keyspacetest");

        CassandraKeyspace keyspace = new CassandraKeyspace(configuration);
        SubscriptionRepository subscriptionRepository = new CassandraSubscriptionRepository(keyspace,configuration);

        ActivityStreamsSubscriberWarehouse warehouse = createMock(ActivityStreamsSubscriberWarehouse.class);
        StreamsSubscriptionRepositoryService repositoryService = new CassandraSubscriptionService(subscriptionRepository);

        streamsSubscriberRegistrationService = new StreamsSubscriberRegistrationServiceImpl(repositoryService, warehouse);
    }

    @Ignore
    @Test
    public void registerTest() throws Exception{
        String subscriberJson = "{\"username\":\"newUsername\"}";

        String inRoute = streamsSubscriberRegistrationService.register(subscriberJson);
        assert(inRoute.equals(streamsSubscriberRegistrationService.register(subscriberJson)));
    }

}
