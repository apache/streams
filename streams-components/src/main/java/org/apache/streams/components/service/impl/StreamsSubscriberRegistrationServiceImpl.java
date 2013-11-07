package org.apache.streams.components.service.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.streams.components.service.StreamsSubscriberRegistrationService;
import org.apache.streams.components.service.StreamsSubscriptionRepositoryService;
import org.apache.streams.components.activitysubscriber.ActivityStreamsSubscriberWarehouse;
import org.apache.streams.persistence.model.ActivityStreamsSubscription;
import org.apache.streams.persistence.model.cassandra.CassandraSubscription;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

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
            ActivityStreamsSubscriberWarehouse activityStreamsSubscriberWarehouse
    ) {
        this.subscriptionRepositoryService = subscriptionRepositoryService;
        this.activityStreamsSubscriberWarehouse = activityStreamsSubscriberWarehouse;
        this.mapper = new ObjectMapper();
        mapper.configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    /**
     * registers the subscriber according to the subscriberJSON
     *
     * @param subscriberJSON the JSON of the subscriber to be registered
     * @return a url that the client can use to GET activity streams
     */
    @Override
    public String register(String subscriberJSON) throws Exception {
        log.info("registering subscriber: "+subscriberJSON);

        ActivityStreamsSubscription subscription = mapper.readValue(subscriberJSON, CassandraSubscription.class);
        ActivityStreamsSubscription fromDB = subscriptionRepositoryService.getSubscriptionByUsername(subscription.getUsername());

        if (fromDB != null) {
            return fromDB.getInRoute();
        } else {
            subscription.setInRoute("" + UUID.randomUUID());
            subscriptionRepositoryService.saveSubscription(subscription);
            activityStreamsSubscriberWarehouse.register(subscription);

            return subscription.getInRoute();
        }
    }
}
