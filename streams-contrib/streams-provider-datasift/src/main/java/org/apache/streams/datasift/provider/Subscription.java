/*
Licensed to the Apache Software Foundation (ASF) under one
or more contributor license agreements.  See the NOTICE file
distributed with this work for additional information
regarding copyright ownership.  The ASF licenses this file
to you under the Apache License, Version 2.0 (the
"License"); you may not use this file except in compliance
with the License.  You may obtain a copy of the License at

  http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing,
software distributed under the License is distributed on an
"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
KIND, either express or implied.  See the License for the
specific language governing permissions and limitations
under the License.
*/

package org.apache.streams.datasift.provider;

import com.datasift.client.core.Stream;
import com.datasift.client.stream.DataSiftMessage;
import com.datasift.client.stream.Interaction;
import com.datasift.client.stream.StreamSubscription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Queue;

/**
 * Adds incomming {@link com.datasift.client.stream.Interaction} to the queue for the provider.
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
