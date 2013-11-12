package org.apache.streams.components.activitysubscriber.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.streams.components.activitysubscriber.ActivityStreamsSubscriber;
import org.apache.streams.components.activitysubscriber.ActivityStreamsSubscriberWarehouse;
import org.apache.streams.components.service.StreamsActivityRepositoryService;
import org.apache.streams.persistence.model.ActivityStreamsSubscription;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class ActivityStreamsSubscriberWarehouseImpl implements ActivityStreamsSubscriberWarehouse {
    private static final transient Log LOG = LogFactory.getLog(ActivityStreamsSubscriberWarehouseImpl.class);

    private Map<String, ActivityStreamsSubscriber> subscribers;
    private StreamsActivityRepositoryService activityService;

    @Autowired
    public ActivityStreamsSubscriberWarehouseImpl(StreamsActivityRepositoryService activityService) {
        this.activityService = activityService;
        subscribers = new HashMap<String, ActivityStreamsSubscriber>();
    }

    @Override
    public void register(ActivityStreamsSubscription subscription) {
        if (!subscribers.containsKey(subscription.getInRoute())) {
            ActivityStreamsSubscriber subscriber = new ActivityStreamsSubscriberDelegate();
            subscribers.put(subscription.getInRoute(), subscriber);
        }
    }

    @Override
    public String getStream(String inRoute) {
        ActivityStreamsSubscriber subscriber = getSubscriber(inRoute);
        if (subscriber != null) {
            return subscriber.getStream();
        } else {
            return "Registration Needed";
        }
    }

    @Override
    public ActivityStreamsSubscriber getSubscriber(String inRoute) {
        return subscribers.get(inRoute);
    }

    @Override
    public synchronized void updateSubscriber(ActivityStreamsSubscription subscription) {
        ActivityStreamsSubscriber subscriber = getSubscriber(subscription.getInRoute());
        if (subscriber != null) {
            //TODO: an activity posted in between the cql query and setting the lastUpdated field will be lost
            Set<String> activities = new TreeSet<String>();
            activities.addAll(activityService.getActivitiesForProviders(subscription.getFilters(), subscriber.getLastUpdated()));
            subscriber.setLastUpdated(new Date());
            subscriber.receive(new ArrayList<String>(activities));
        }
    }
}
