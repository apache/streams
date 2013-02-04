package org.apache.streams.osgi.components.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.streams.osgi.components.ActivityStreamsSubscriberRegistration;
import org.apache.streams.osgi.components.activitysubscriber.impl.ActivityStreamsSubscriberDelegate;

import java.util.Date;
import java.util.HashMap;

public class ActivityStreamsSubscriberRegistrationImpl implements ActivityStreamsSubscriberRegistration {
    private static final transient Log LOG = LogFactory.getLog(ActivityStreamsSubscriberRegistrationImpl.class);
    private boolean verbose = true;
    private String prefix = "Activity Subscriber Registration";

    public Object register(Object body) {

        //authorize this subscriber based on some rule set...
        //create a new SubscriberDelegate...
        //using the URI supplied to set it up...
        //return the consumer for addition to the consumer warehouse

        HashMap<String,String[]> bodyParts = parseBody(body.toString());
        String answer = prefix + " set body:  " + body + " " + new Date();
        LOG.info(">> setting up subscriptions >>" + bodyParts.get("subscriptions"));
        if (bodyParts.get("subscriptions")!=null){return new ActivityStreamsSubscriberDelegate(bodyParts.get("subscriptions"));}
        return new ActivityStreamsSubscriberDelegate();
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

    public boolean isVerbose() {
        return verbose;
    }

    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }
}
