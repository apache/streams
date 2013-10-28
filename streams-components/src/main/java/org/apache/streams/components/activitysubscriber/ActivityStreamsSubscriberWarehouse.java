package org.apache.streams.components.activitysubscriber;

import org.apache.streams.persistence.model.ActivityStreamsSubscription;

/**
 * Public API representing an example OSGi service
 */
public interface ActivityStreamsSubscriberWarehouse {

    String getStream(String inRoute);
    ActivityStreamsSubscriber getSubscriber(String inRoute);
    void updateSubscriber(ActivityStreamsSubscription subscription);
    void register(ActivityStreamsSubscription subscription);
}

