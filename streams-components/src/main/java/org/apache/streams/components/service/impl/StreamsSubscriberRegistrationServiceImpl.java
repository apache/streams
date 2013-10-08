package org.apache.streams.components.service.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.streams.components.service.StreamsSubscriberRegistrationService;
import org.apache.streams.components.aggregation.ActivityAggregator;
import org.apache.streams.components.configuration.StreamsConfiguration;
import org.apache.streams.components.service.StreamsSubscriptionRepositoryService;
import org.apache.streams.components.activitysubscriber.ActivityStreamsSubscriber;
import org.apache.streams.components.activitysubscriber.ActivityStreamsSubscriberWarehouse;
import org.apache.streams.persistence.model.ActivityStreamsSubscription;
import org.apache.streams.components.activitysubscriber.impl.ActivityStreamsSubscriberDelegate;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.UUID;

@Component
public class StreamsSubscriberRegistrationServiceImpl implements StreamsSubscriberRegistrationService {
    private Log log = LogFactory.getLog(StreamsSubscriberRegistrationServiceImpl.class);

    private StreamsSubscriptionRepositoryService subscriptionService;
    private ActivityAggregator activityAggregator;
    private ActivityStreamsSubscriberWarehouse activityStreamsSubscriberWarehouse;
    private StreamsConfiguration configuration;

    @Autowired
    public StreamsSubscriberRegistrationServiceImpl(
            StreamsSubscriptionRepositoryService subscriptionService,
            ActivityAggregator activityAggregator,
            ActivityStreamsSubscriberWarehouse activityStreamsSubscriberWarehouse,
            StreamsConfiguration configuration
    ) {
        this.subscriptionService = subscriptionService;
        this.activityAggregator = activityAggregator;
        this.activityStreamsSubscriberWarehouse = activityStreamsSubscriberWarehouse;
        this.configuration = configuration;
    }

    /**
     * registers the subscriber according to the subscriberJSON
     * @param subscriberJSON the JSON of the subscriber to be registered
     * @return a url that the client can use to GET activity streams
     * */
    public String register(String subscriberJSON) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        ActivityStreamsSubscription subscription = mapper.readValue(subscriberJSON, ActivityStreamsSubscription.class);
        if (subscription.getFilters() == null) {
            subscription.setFilters(subscriptionService.getFilters(subscription.getAuthToken()));
        } else {
            subscriptionService.saveFilters(subscription);
        }

        ActivityStreamsSubscriber subscriber = new ActivityStreamsSubscriberDelegate(subscription);
        subscriber.setAuthenticated(true);
        subscriber.setInRoute("" + UUID.randomUUID());
        activityAggregator.updateSubscriber(subscriber);
        activityStreamsSubscriberWarehouse.register(subscriber);

        return configuration.getBaseUrlPath() + "getActivity/" + subscriber.getInRoute();
    }
}
