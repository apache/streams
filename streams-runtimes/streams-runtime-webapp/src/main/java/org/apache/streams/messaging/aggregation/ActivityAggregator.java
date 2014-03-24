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

package org.apache.streams.messaging.aggregation;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.streams.messaging.service.impl.CassandraActivityService;
import org.apache.streams.osgi.components.activitysubscriber.ActivityStreamsSubscriber;
import org.apache.streams.osgi.components.activitysubscriber.ActivityStreamsSubscriberWarehouse;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.*;

public class ActivityAggregator {

    private ActivityStreamsSubscriberWarehouse activityStreamsSubscriberWarehouse;
    private CassandraActivityService activityService;
    private static final transient Log LOG = LogFactory.getLog(ActivityAggregator.class);

    public void setActivityStreamsSubscriberWarehouse(ActivityStreamsSubscriberWarehouse activityStreamsSubscriberWarehouse) {
        this.activityStreamsSubscriberWarehouse = activityStreamsSubscriberWarehouse;
    }

    public void setActivityService(CassandraActivityService activityService) {
        this.activityService = activityService;
    }

    @Scheduled(fixedRate=30000)
    public void distributeToSubscribers() {
        for (ActivityStreamsSubscriber subscriber : activityStreamsSubscriberWarehouse.getAllSubscribers()) {
              updateSubscriber(subscriber);
        }
    }

    public void updateSubscriber(ActivityStreamsSubscriber subscriber){
        Set<String> activities = new TreeSet<String>();
        activities.addAll(activityService.getActivitiesForFilters(subscriber.getActivityStreamsSubscriberConfiguration().getFilters(), subscriber.getLastUpdated()));
        //TODO: an activity posted in between the cql query and setting the lastUpdated field will be lost
        subscriber.setLastUpdated(new Date());
        subscriber.receive(new ArrayList<String>(activities));
    }
}
