package org.apache.streams.datasift.provider;

import com.datasift.client.core.Stream;
import com.datasift.client.stream.Interaction;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import static org.junit.Assert.assertEquals;

/**
 * Requires Java version 1.7!
 */
public class SubscriptionTest {

    @Test
    public void testSubscriptionOnMessage() {
        Stream mockStream = Mockito.mock(Stream.class);
        Mockito.when(mockStream.hash()).thenReturn("1");
        Queue<Interaction> interactionQueue = new ConcurrentLinkedQueue<Interaction>();
        Subscription subscriptionStub = new Subscription(mockStream, interactionQueue);
        addInteractions(1, subscriptionStub);
        assertEquals(1, interactionQueue.size());
        addInteractions(30, subscriptionStub);
        assertEquals(31, interactionQueue.size());
        while(!interactionQueue.isEmpty())
            interactionQueue.poll();
        addInteractions(5, subscriptionStub);
        assertEquals(5, interactionQueue.size());
    }

    private void addInteractions(int numToAdd, Subscription subscription) {
        for(int i=0; i < numToAdd; ++i) {
            subscription.onMessage(Mockito.mock(Interaction.class));
        }
    }

}
