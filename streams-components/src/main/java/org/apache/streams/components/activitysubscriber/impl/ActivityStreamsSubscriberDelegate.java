package org.apache.streams.components.activitysubscriber.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.streams.components.activitysubscriber.ActivityStreamsSubscriber;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ActivityStreamsSubscriberDelegate implements ActivityStreamsSubscriber {

    private static final transient Log LOG = LogFactory.getLog(ActivityStreamsSubscriberDelegate.class);

    //an individual subscriber gets ONE stream which is an aggregation of all its SRCs
    private List<String> stream;

    private Date lastUpdated;

    public ActivityStreamsSubscriberDelegate(){
        this.stream = new ArrayList<String>();
        this.lastUpdated = new Date(0);
    }

    //return the list of activities (stream) as a json string
    public String getStream() {

        return stream.toString();
    }

    public Date getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(Date lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    public void receive(List<String> activities){
        stream.addAll(activities);
    }
}
