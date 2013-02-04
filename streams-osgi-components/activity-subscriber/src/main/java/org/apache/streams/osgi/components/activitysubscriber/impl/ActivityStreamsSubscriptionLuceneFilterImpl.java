package org.apache.streams.osgi.components.activitysubscriber.impl;


import org.apache.streams.osgi.components.activitysubscriber.ActivityStreamsSubscriptionFilter;

public class ActivityStreamsSubscriptionLuceneFilterImpl implements ActivityStreamsSubscriptionFilter{

    private String query;



    public void setQuery(String query) {
        this.query=query;
    }

    public boolean evaluate(String activity){
        return true;
    }


}
