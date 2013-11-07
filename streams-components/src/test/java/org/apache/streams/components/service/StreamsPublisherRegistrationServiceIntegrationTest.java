package org.apache.streams.components.service;

import org.apache.streams.components.activitysubscriber.ActivityStreamsSubscriberWarehouse;
import org.apache.streams.components.service.impl.CassandraPublisherService;
import org.apache.streams.components.service.impl.CassandraSubscriptionService;
import org.apache.streams.components.service.impl.StreamsPublisherRegistrationServiceImpl;
import org.apache.streams.components.service.impl.StreamsSubscriberRegistrationServiceImpl;
import org.apache.streams.persistence.configuration.CassandraConfiguration;
import org.apache.streams.persistence.repository.PublisherRepository;
import org.apache.streams.persistence.repository.SubscriptionRepository;
import org.apache.streams.persistence.repository.cassandra.CassandraKeyspace;
import org.apache.streams.persistence.repository.cassandra.CassandraPublisherRepository;
import org.apache.streams.persistence.repository.cassandra.CassandraSubscriptionRepository;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import static org.easymock.EasyMock.createMock;

public class StreamsPublisherRegistrationServiceIntegrationTest {
    private StreamsPublisherRegistrationService streamsPublisherRegistrationService;

    @Before
    public void setup(){

        CassandraConfiguration configuration = new CassandraConfiguration();
        configuration.setCassandraPort("127.0.0.1");
        configuration.setPublisherColumnFamilyName("publishertestD");
        configuration.setKeyspaceName("keyspacetest");

        CassandraKeyspace keyspace = new CassandraKeyspace(configuration);
        PublisherRepository publisherRepository = new CassandraPublisherRepository(keyspace,configuration);

        StreamsPublisherRepositoryService publisherRepositoryService = new CassandraPublisherService(publisherRepository);

        streamsPublisherRegistrationService = new StreamsPublisherRegistrationServiceImpl(publisherRepositoryService);
    }

    @Ignore
    @Test
    public void registerTest() throws Exception{
        String subscriberJson = "{\"src\":\"example.com\"}";

        String inRoute = streamsPublisherRegistrationService.register(subscriberJson);
        assert(inRoute.equals(streamsPublisherRegistrationService.register(subscriberJson)));
    }
}
