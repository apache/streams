package org.apache.streams.components.activitysubscriber.impl;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.streams.components.activitysubscriber.ActivityStreamsSubscriber;
import org.apache.streams.components.activitysubscriber.ActivityStreamsSubscriberWarehouse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ActivityStreamsSubscriberWarehouseImpl implements ActivityStreamsSubscriberWarehouse {
    private static final transient Log LOG = LogFactory.getLog(ActivityStreamsSubscriberWarehouseImpl.class);

    private Map<String, ActivityStreamsSubscriber> subscribers;

    public ActivityStreamsSubscriberWarehouseImpl(){
        subscribers = new HashMap<String, ActivityStreamsSubscriber>();
    }

    public void register(ActivityStreamsSubscriber activitySubscriber) {
        if (!subscribers.containsKey(activitySubscriber.getInRoute())){
            subscribers.put(activitySubscriber.getInRoute(), activitySubscriber);
            activitySubscriber.init();
        }

    }

    //the warehouse can do some interesting things to make the filtering efficient i think...
    public ActivityStreamsSubscriber findSubscribersByID(String id){
        return subscribers.get(id);
    }


    public Collection<ActivityStreamsSubscriber> getAllSubscribers(){
        return subscribers.values();
    }



}
