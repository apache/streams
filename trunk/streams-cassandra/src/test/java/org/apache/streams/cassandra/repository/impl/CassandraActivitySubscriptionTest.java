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

package org.apache.streams.cassandra.repository.impl;

import org.apache.streams.osgi.components.activitysubscriber.ActivityStreamsSubscription;
import org.apache.streams.osgi.components.activitysubscriber.impl.ActivityStreamsSubscriptionImpl;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.util.Arrays;

public class CassandraActivitySubscriptionTest {

    public CassandraSubscriptionRepository repository;


    @Before
    public void setup() {
//        repository = new CassandraSubscriptionRepository();
    }

    @Ignore
    @Test
    public void saveTest(){
        ActivityStreamsSubscription subscription = new ActivityStreamsSubscriptionImpl();
        subscription.setFilters(Arrays.asList("thisis", "atest"));
        subscription.setAuthToken("subid");

        repository.save(subscription);
    }

    @Ignore
    @Test
    public void getTest(){
        String filters = repository.getFilters("subid");
    }
}
