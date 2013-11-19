package org.apache.streams.components.service.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.streams.components.activitysubscriber.ActivityStreamsSubscriberWarehouse;
import org.apache.streams.components.service.StreamsSubscriberRegistrationService;
import org.apache.streams.persistence.model.ActivityStreamsSubscription;
import org.apache.streams.persistence.model.cassandra.CassandraSubscription;
import org.apache.streams.persistence.model.mongo.MongoSubscription;
import org.apache.streams.persistence.repository.SubscriptionRepository;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class StreamsSubscriberRegistrationServiceImpl implements StreamsSubscriberRegistrationService {
    private Log log = LogFactory.getLog(StreamsSubscriberRegistrationServiceImpl.class);

    private SubscriptionRepository subscriptionRepository;
    private ActivityStreamsSubscriberWarehouse activityStreamsSubscriberWarehouse;
    private Class subscriptionClass;
    private ObjectMapper mapper;

    @Autowired
    public StreamsSubscriberRegistrationServiceImpl(
            SubscriptionRepository subscriptionRepository,
            ActivityStreamsSubscriberWarehouse activityStreamsSubscriberWarehouse,
            Class subscriptionClass
    ) {
        this.subscriptionRepository = subscriptionRepository;
        this.activityStreamsSubscriberWarehouse = activityStreamsSubscriberWarehouse;
        this.mapper = new ObjectMapper();
        this.subscriptionClass = subscriptionClass;
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

        ActivityStreamsSubscription subscription = (ActivityStreamsSubscription)mapper.readValue(subscriberJSON, subscriptionClass);
        ActivityStreamsSubscription fromDB = subscriptionRepository.getSubscriptionByUsername(subscription.getUsername());

        if (fromDB != null) {
            return fromDB.getInRoute();
        } else {
            subscription.setInRoute("" + UUID.randomUUID());
            subscriptionRepository.save(subscription);
            activityStreamsSubscriberWarehouse.register(subscription);

            return subscription.getInRoute();
        }
    }
}
