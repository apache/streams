package org.apache.streams.osgi.components.activitysubscriber;

import org.codehaus.jackson.annotate.JsonTypeInfo;

import java.util.ArrayList;
@JsonTypeInfo(use= JsonTypeInfo.Id.CLASS, include=JsonTypeInfo.As.PROPERTY, property="@class")
public interface ActivityStreamsSubscription {

    public ArrayList<ActivityStreamsSubscriptionFilter> getActivityStreamsSubscriptionFilters();
    public void setActivityStreamsSubscriptionFilters(ArrayList<ActivityStreamsSubscriptionFilter> filters);

    public ArrayList<ActivityStreamsSubscriptionOutput> getActivityStreamsSubscriptionOutputs();
    public void setActivityStreamsSubscriptionOutputs(ArrayList<ActivityStreamsSubscriptionOutput> outputs);

    public String getAuthToken();
    public void setAuthToken(String token);

}
