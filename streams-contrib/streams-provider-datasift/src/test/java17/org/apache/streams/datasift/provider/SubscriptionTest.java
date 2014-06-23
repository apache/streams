/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

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
