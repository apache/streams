package org.apache.streams.osgi.components.activityconsumer;

/**
 * Public API representing an example OSGi service
 */
public interface ActivityConsumerWarehouse {

    public void register(ActivityConsumer activityConsumer);
    public ActivityConsumer findConsumerBySrc(String src);
    public int getConsumersCount();

}

