package org.apache.streams.components.activityconsumer;

/**
 * Public API representing an example OSGi service
 */
public interface ActivityConsumerWarehouse {

    public void register(ActivityConsumer activityConsumer);
    public ActivityConsumer findConsumer(String id);
    public int getConsumersCount();

}

