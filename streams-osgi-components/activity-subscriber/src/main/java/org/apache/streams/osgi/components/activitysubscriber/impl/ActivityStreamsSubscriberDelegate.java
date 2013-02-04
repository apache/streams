package org.apache.streams.osgi.components.activitysubscriber.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.streams.osgi.components.activitysubscriber.ActivityStreamsSubscriber;
import org.apache.streams.osgi.components.activitysubscriber.ActivityStreamsSubscriberConfiguration;
import org.apache.streams.osgi.components.activitysubscriber.ActivityStreamsSubscriberWarehouse;

import java.util.ArrayList;
import java.util.HashMap;

public class ActivityStreamsSubscriberDelegate implements ActivityStreamsSubscriber {

    private static final transient Log LOG = LogFactory.getLog(ActivityStreamsSubscriberDelegate.class);


    private ActivityStreamsSubscriberConfiguration activityStreamsSubscriberConfiguration;

    private String inRoute;

    //an individual subscriber gets ONE stream which is an aggregation of all its SRCs
    private ArrayList<String> stream;


    public ActivityStreamsSubscriberDelegate(ActivityStreamsSubscriberConfiguration configuration){
        setActivityStreamsSubscriberConfiguration(configuration);
        stream = new ArrayList<String>();
    }


    public ActivityStreamsSubscriberConfiguration getActivityStreamsSubscriberConfiguration() {
        return activityStreamsSubscriberConfiguration;
    }

    public void setActivityStreamsSubscriberConfiguration(ActivityStreamsSubscriberConfiguration activityStreamsSubscriberConfiguration) {
        this.activityStreamsSubscriberConfiguration = activityStreamsSubscriberConfiguration;
    }

    public void updateActivityStreamsSubscriberConfiguration(ActivityStreamsSubscriberConfiguration activityStreamsSubscriberConfiguration) {
        this.activityStreamsSubscriberConfiguration = activityStreamsSubscriberConfiguration;
    }

    public String getInRoute() {
        return inRoute;
    }

    public void setInRoute(String inRoute) {
        this.inRoute = inRoute;
    }

    public void receive (String activity){
        //receive activities...do anything that is necessary
        LOG.info("got a message i subscribed to: " + activity);
        //guarenteed unique?
        stream.add(activity);

    }

    //return the list of activities (stream) as a json string
    public String getStream() {

        return stream.toString();
    }



    public void init(){
        //any initialization... gets called directly after registration



    }





}
