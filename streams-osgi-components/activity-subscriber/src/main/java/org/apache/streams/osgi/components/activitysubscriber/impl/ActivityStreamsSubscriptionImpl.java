package org.apache.streams.osgi.components.activitysubscriber.impl;


import org.apache.streams.osgi.components.activitysubscriber.ActivityStreamsSubscription;
import org.apache.streams.osgi.components.activitysubscriber.ActivityStreamsSubscriptionFilter;
import org.apache.streams.osgi.components.activitysubscriber.ActivityStreamsSubscriptionOutput;

import java.util.ArrayList;

public class ActivityStreamsSubscriptionImpl implements ActivityStreamsSubscription {

    private  ArrayList<ActivityStreamsSubscriptionFilter> activityStreamsSubscriptionFilters;
    private ArrayList<ActivityStreamsSubscriptionOutput> activityStreamsSubscriptionOutputs;




    private String authToken;

    @Override
    public ArrayList<ActivityStreamsSubscriptionFilter> getActivityStreamsSubscriptionFilters() {
        return activityStreamsSubscriptionFilters;
    }

    @Override
    public void setActivityStreamsSubscriptionFilters(ArrayList<ActivityStreamsSubscriptionFilter> filters) {
        this.activityStreamsSubscriptionFilters = filters;
    }

    @Override
    public ArrayList<ActivityStreamsSubscriptionOutput> getActivityStreamsSubscriptionOutputs() {
        return activityStreamsSubscriptionOutputs;
    }

    @Override
    public void setActivityStreamsSubscriptionOutputs(ArrayList<ActivityStreamsSubscriptionOutput> outputs) {
        this.activityStreamsSubscriptionOutputs = outputs;
    }

    public String getAuthToken() {
        return authToken;
    }

    public void setAuthToken(String auth_token) {
        this.authToken = auth_token;
    }





}
