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

package org.apache.streams.messaging.service.impl;

/*
 * #%L
 * streams-eip-routes [org.apache.streams]
 * %%
 * Copyright (C) 2013 - 2014 Apache Streams Project
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import org.apache.streams.cassandra.repository.impl.CassandraSubscriptionRepository;
import org.apache.streams.messaging.service.SubscriptionService;
import org.apache.streams.osgi.components.activitysubscriber.ActivityStreamsSubscription;

import java.util.Arrays;
import java.util.List;

public class CassandraSubscriptionService implements SubscriptionService {

    private CassandraSubscriptionRepository repository;

    public CassandraSubscriptionService(CassandraSubscriptionRepository repository){
        this.repository = repository;
    }

    public List<String> getFilters(String authToken){
          return Arrays.asList(repository.getFilters(authToken).split(" "));
    }

    public void saveFilters(ActivityStreamsSubscription subscription){
          repository.save(subscription);
    }
}
