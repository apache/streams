package org.apache.streams.osgi.components.activitysubscriber.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.streams.osgi.components.activitysubscriber.ActivityStreamsSubscriber;
import org.apache.streams.osgi.components.activitysubscriber.ActivityStreamsSubscriberWarehouse;

import java.util.ArrayList;
import java.util.HashMap;

public class ActivityStreamsSubscriberDelegate implements ActivityStreamsSubscriber {

    private static final transient Log LOG = LogFactory.getLog(ActivityStreamsSubscriberDelegate.class);



    private ActivityStreamsSubscriberWarehouse activityStreamsSubscriberWarehouse;

    private String inRoute;

    //an individual subscriber gets ONE stream which is an aggregation of all its SRCs
    private ArrayList<String> stream;



    private String[] subscriptions;

    public ActivityStreamsSubscriberDelegate(){

        stream = new ArrayList<String>();
    }

    public ActivityStreamsSubscriberDelegate(String[] subscriptions){

        this();
        this.subscriptions=subscriptions;

    }

    public void setActivityStreamsSubscriberWarehouse(ActivityStreamsSubscriberWarehouse activityStreamsSubscriberWarehouse) {
        this.activityStreamsSubscriberWarehouse = activityStreamsSubscriberWarehouse;
    }


    public String getInRoute() {
        return inRoute;
    }

    public void setInRoute(String inRoute) {
        this.inRoute = inRoute;
    }

    public String[] getSubscriptions() {
        return subscriptions;
    }

    public void addSrc(String src){
        HashMap<String,String[]> bodyParts = parseBody(src);

        activityStreamsSubscriberWarehouse.register(bodyParts.get("subscriptions"),this);
    }

    public void addSrc(String[] src){

        activityStreamsSubscriberWarehouse.register(src,this);
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

    private HashMap<String, String[]> parseBody(String body) {
        HashMap<String,String[]> parts = new HashMap<String, String[]>();
        String[] segments = body.split("&");
        for (String seg : segments){
            String[] query = seg.split("=");
            if (query.length>0) {
                parts.put(query[0],query[1].split(","));
            }
        }

        if (parts.isEmpty()){return null;}
        return parts;
    }



}
