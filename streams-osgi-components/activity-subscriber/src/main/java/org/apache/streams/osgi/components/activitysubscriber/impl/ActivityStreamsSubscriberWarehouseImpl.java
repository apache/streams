package org.apache.streams.osgi.components.activitysubscriber.impl;

import java.util.ArrayList;
import java.util.HashMap;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.streams.osgi.components.activitysubscriber.ActivityStreamsSubscriber;
import org.apache.streams.osgi.components.activitysubscriber.ActivityStreamsSubscriberWarehouse;


public class ActivityStreamsSubscriberWarehouseImpl implements ActivityStreamsSubscriberWarehouse {
    private static final transient Log LOG = LogFactory.getLog(ActivityStreamsSubscriberWarehouseImpl.class);

    private ArrayList<ActivityStreamsSubscriber> subscribers;

    public ActivityStreamsSubscriberWarehouseImpl(){
        subscribers = new ArrayList<ActivityStreamsSubscriber>();
    }

    public void register(ActivityStreamsSubscriber activitySubscriber) {

        if (!subscribers.contains(activitySubscriber)){
            subscribers.add(activitySubscriber);
            activitySubscriber.init();
        }

    }


    //the warehouse can do some interesting things to make the filtering efficient i think...
    public ArrayList<ActivityStreamsSubscriber> findSubscribersByFilters(String src){
        return subscribers;
    }


    public ArrayList<ActivityStreamsSubscriber> getAllSubscribers(){
        return subscribers;
    }



}
