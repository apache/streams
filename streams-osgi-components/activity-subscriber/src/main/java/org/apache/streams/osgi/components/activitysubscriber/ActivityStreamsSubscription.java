package org.apache.streams.osgi.components.activitysubscriber;

import java.util.ArrayList;

public interface ActivityStreamsSubscription {

    public ArrayList<ActivityStreamsSubscriptionFilter> getActivityStreamsSubscriptionFilters();
    public void setActivityStreamsSubscriptionFilters(ArrayList<ActivityStreamsSubscriptionFilter> filters);

    public ArrayList<ActivityStreamsSubscriptionOutput> getActivityStreamsSubscriptionOutputs();
    public void setActivityStreamsSubscriptionOutputs(ArrayList<ActivityStreamsSubscriptionOutput> outputs);

}
