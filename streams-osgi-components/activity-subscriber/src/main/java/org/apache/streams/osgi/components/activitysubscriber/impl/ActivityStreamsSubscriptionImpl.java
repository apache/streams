package org.apache.streams.osgi.components.activitysubscriber.impl;


import org.apache.streams.osgi.components.activitysubscriber.ActivityStreamsSubscription;
import org.apache.streams.osgi.components.activitysubscriber.ActivityStreamsSubscriptionOutput;

import org.codehaus.jackson.map.annotate.JsonDeserialize;

import java.util.ArrayList;
import java.util.List;

public class ActivityStreamsSubscriptionImpl implements ActivityStreamsSubscription {

    @JsonDeserialize(as=ArrayList.class)
    private List<String> filters;

    @JsonDeserialize(as=ArrayList.class)
    private List<ActivityStreamsSubscriptionOutput> outputs;

    private String authToken;

    public void setFilters(List<String> filters) {
        //TODO: it's possible that this could be null
        this.filters = filters;
    }

    @Override
    public List<ActivityStreamsSubscriptionOutput> getActivityStreamsSubscriptionOutputs() {
        return outputs;
    }

    @Override
    public void setActivityStreamsSubscriptionOutputs(List<ActivityStreamsSubscriptionOutput> outputs) {
        this.outputs = outputs;
    }

    @Override
    public List<String> getFilters(){
        return filters;

    }

    @Override
    public String getAuthToken() {
        return authToken;
    }

    @Override
    public void setAuthToken(String auth_token) {
        this.authToken = auth_token;
    }





}
