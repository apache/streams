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

package org.apache.streams.osgi.components.activitysubscriber.impl;

import java.util.ArrayList;
import java.util.HashMap;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.streams.osgi.components.activitysubscriber.ActivityStreamsSubscriber;
import org.apache.streams.osgi.components.activitysubscriber.ActivityStreamsSubscriberWarehouse;


public class ActivityStreamsSubscriberWarehouseImpl implements ActivityStreamsSubscriberWarehouse {
    private static final transient Log LOG = LogFactory.getLog(ActivityStreamsSubscriberWarehouseImpl.class);

    private ArrayList<ActivityStreamsSubscriber> subscribers;

    public ActivityStreamsSubscriberWarehouseImpl(){
        subscribers = new ArrayList<ActivityStreamsSubscriber>();
    }

    public void register(ActivityStreamsSubscriber activitySubscriber) {

        if (!subscribers.contains(activitySubscriber)){
            subscribers.add(activitySubscriber);
            activitySubscriber.init();
        }

    }


    //the warehouse can do some interesting things to make the filtering efficient i think...
    public ArrayList<ActivityStreamsSubscriber> findSubscribersByFilters(String src){
        return subscribers;
    }


    public ArrayList<ActivityStreamsSubscriber> getAllSubscribers(){
        return subscribers;
    }



}
