package org.apache.streams.components.service.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.streams.components.service.StreamsSubscriberRegistrationService;
import org.apache.streams.components.service.StreamsSubscriptionRepositoryService;
import org.apache.streams.components.activitysubscriber.ActivityStreamsSubscriberWarehouse;
import org.apache.streams.persistence.model.ActivityStreamsSubscription;
import org.apache.streams.persistence.model.cassandra.CassandraSubscription;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.UUID;

@Component
public class StreamsSubscriberRegistrationServiceImpl implements StreamsSubscriberRegistrationService {
    private Log log = LogFactory.getLog(StreamsSubscriberRegistrationServiceImpl.class);

    private StreamsSubscriptionRepositoryService subscriptionRepositoryService;
    private ActivityStreamsSubscriberWarehouse activityStreamsSubscriberWarehouse;
    private ObjectMapper mapper;

    @Autowired
    public StreamsSubscriberRegistrationServiceImpl(
            StreamsSubscriptionRepositoryService subscriptionRepositoryService,
            ActivityStreamsSubscriberWarehouse activityStreamsSubscriberWarehouse,
            ObjectMapper mapper
    ) {
        this.subscriptionRepositoryService = subscriptionRepositoryService;
        this.activityStreamsSubscriberWarehouse = activityStreamsSubscriberWarehouse;
        this.mapper = mapper;
    }

    /**
     * registers the subscriber according to the subscriberJSON
     * @param subscriberJSON the JSON of the subscriber to be registered
     * @return a url that the client can use to GET activity streams
     * */
    public String register(String subscriberJSON) throws IOException {
        ActivityStreamsSubscription subscription = mapper.readValue(subscriberJSON, CassandraSubscription.class);
        subscription.setInRoute("" + UUID.randomUUID());
        subscriptionRepositoryService.saveSubscription(subscription);
        activityStreamsSubscriberWarehouse.register(subscription);

        return subscription.getInRoute();
    }
}
