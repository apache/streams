package org.apache.streams.osgi.components.activitysubscriber;

import org.codehaus.jackson.annotate.JsonTypeInfo;

import java.util.ArrayList;
import java.util.List;

@JsonTypeInfo(use= JsonTypeInfo.Id.CLASS, include=JsonTypeInfo.As.PROPERTY, property="@class")
public interface ActivityStreamsSubscription {

    public void setFilters(List<String> filters);
    public List<String> getFilters();

    public List<ActivityStreamsSubscriptionOutput> getActivityStreamsSubscriptionOutputs();
    public void setActivityStreamsSubscriptionOutputs(List<ActivityStreamsSubscriptionOutput> outputs);

    public String getAuthToken();
    public void setAuthToken(String token);

}
