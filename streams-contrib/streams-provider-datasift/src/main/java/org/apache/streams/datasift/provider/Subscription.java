package org.apache.streams.datasift.provider;

import com.datasift.client.core.Stream;
import com.datasift.client.stream.DataSiftMessage;
import com.datasift.client.stream.Interaction;
import com.datasift.client.stream.StreamSubscription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Queue;

/**
 * Created by rebanks on 5/19/14.
 */
public class Subscription extends StreamSubscription {

    private static final Logger LOGGER = LoggerFactory.getLogger(Subscription.class);
    private Queue<Interaction> sharedQueue;

    public Subscription(Stream stream, Queue<Interaction> sharedQueue) {
        super(stream);
        this.sharedQueue = sharedQueue;
    }

    @Override
    public void onDataSiftLogMessage(DataSiftMessage dataSiftMessage) {
        if (dataSiftMessage.isError()) //should we restart the subscription on error?
            LOGGER.error("Datasift Error : {}", dataSiftMessage.getMessage());
        else if (dataSiftMessage.isWarning())
            LOGGER.warn("Datasift Warning : {}", dataSiftMessage.getMessage());
        else
            LOGGER.info("Datasift Info : {}", dataSiftMessage.getMessage());
    }

    @Override
    public void onMessage(Interaction interaction) {
        while (!this.sharedQueue.offer(interaction)) {
            Thread.yield();
        }
    }
}
