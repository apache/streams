package org.apache.streams.osgi.components.activitysubscriber.impl;

import java.util.ArrayList;
import java.util.HashMap;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.streams.osgi.components.activitysubscriber.ActivityStreamsSubscriber;
import org.apache.streams.osgi.components.activitysubscriber.ActivityStreamsSubscriberWarehouse;


public class ActivityStreamsSubscriberWarehouseImpl implements ActivityStreamsSubscriberWarehouse {
    private static final transient Log LOG = LogFactory.getLog(ActivityStreamsSubscriberWarehouseImpl.class);

    private HashMap<String,ArrayList<ActivityStreamsSubscriber>> subscribers;

    public ActivityStreamsSubscriberWarehouseImpl(){
        subscribers = new HashMap<String, ArrayList<ActivityStreamsSubscriber>>();
        subscribers.put(null,new ArrayList<ActivityStreamsSubscriber>());
    }

    public void register(String src, ActivityStreamsSubscriber activitySubscriber) {

        ArrayList<ActivityStreamsSubscriber> registeredSubscribers = subscribers.get(src);

        if (registeredSubscribers==null){
            registeredSubscribers = new ArrayList<ActivityStreamsSubscriber>();
        }

        registeredSubscribers.add(activitySubscriber);
        subscribers.put(src,registeredSubscribers);
        activitySubscriber.init();


    }

    public void register(String[] src, ActivityStreamsSubscriber activitySubscriber) {

       for (String s : src){
           register(s,activitySubscriber);
       }


    }



    public ArrayList<ActivityStreamsSubscriber> findSubscribersBySrc(String src){
        return subscribers.get(src);
    }



}
