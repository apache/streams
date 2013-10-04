package org.apache.streams.osgi.components.activitysubscriber.impl;

import org.apache.streams.osgi.components.activitysubscriber.ActivityStreamsSubscriptionFilter;

import java.util.List;

//TODO: delete this
public class ActivityStreamsSubscriptionCassandraFilterImpl implements ActivityStreamsSubscriptionFilter {
    private String query;
    private List<String> filters;

    public ActivityStreamsSubscriptionCassandraFilterImpl(){}

    public void setQuery(String query) {
        this.query=query;
    }

    public String getQuery() {
        return query;
    }

    public boolean evaluate(String activity){
        return true;
    }
}
