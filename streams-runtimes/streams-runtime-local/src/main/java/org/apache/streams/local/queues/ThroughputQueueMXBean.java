package org.apache.streams.local.queues;

import javax.management.MXBean;

/**
 * MXBean capable queue that monitors the throughput of the queue
 */
public interface ThroughputQueueMXBean {

    /**
     * Returns the number of items on the queue.
     * @return number of items on queue
     */ 
    public long getCurrentSize();

    /**
     * Get the average time an item spends in queue in milliseconds
     * @return average time an item spends in queue in milliseconds
     */
    public double getAvgWait();

    /**
     * Get the maximum time an item has spent on the queue before being removed from the queue.
     * @return the maximum time an item has spent on the queue
     */
    public long getMaxWait();

    /**
     * Get the number of items that have been removed from this queue
     * @return number of items that have been removed from the queue
     */
    public long getRemoved();

    /**
     * Get the number of items that have been added to the queue
     * @return number of items that have been added to the queue
     */
    public long getAdded();

    /**
     * Get the the throughput of the queue measured by the number of items removed from the queue
     * dived by the time the queue has been active.
     * Active time starts once the first item has been placed on the queue
     * @return throughput of queue. items/sec, items removed / time active
     */
    public double getThroughput();


}
