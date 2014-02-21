package org.apache.streams.osgi.components.activitysubscriber;

import java.util.ArrayList;

/**
 * Public API representing an example OSGi service
 */
public interface ActivityStreamsSubscriberWarehouse {

    public void register(ActivityStreamsSubscriber activitySubscriber);

    public ArrayList<ActivityStreamsSubscriber> findSubscribersByFilters(String src);

    public ArrayList<ActivityStreamsSubscriber> getAllSubscribers();
}

