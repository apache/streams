package org.apache.streams.osgi.components.activityconsumer.impl;

/*
 * #%L
 * activity-consumer-bundle [org.apache.streams.osgi.components.activityconsumer]
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

import java.util.Date;
import java.util.HashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.streams.osgi.components.activityconsumer.ActivityConsumerWarehouse;
import org.apache.streams.osgi.components.activityconsumer.ActivityConsumer;

public class ActivityConsumerWarehouseImpl implements ActivityConsumerWarehouse {
    private static final transient Log LOG = LogFactory.getLog(ActivityConsumerWarehouseImpl.class);

    private HashMap<String,ActivityConsumer> consumers;

    public ActivityConsumerWarehouseImpl(){
        consumers = new HashMap<String, ActivityConsumer>();
    }

    public void register(ActivityConsumer activityConsumer) {

        //key in warehouse is the activity publisher URI source
        consumers.put(activityConsumer.getSrc().toASCIIString(), activityConsumer);
        activityConsumer.init();


    }

    public ActivityConsumer findConsumerBySrc(String src){
        return consumers.get(src);
    }


    public int getConsumersCount(){
        return consumers.size();
    }


}
