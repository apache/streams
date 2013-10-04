package org.apache.streams.osgi.components.activitysubscriber;

import java.util.Collection;

/**
 * Public API representing an example OSGi service
 */
public interface ActivityStreamsSubscriberWarehouse {

    public void register(ActivityStreamsSubscriber activitySubscriber);

    public ActivityStreamsSubscriber findSubscribersByID(String id);

    public Collection<ActivityStreamsSubscriber> getAllSubscribers();
}

