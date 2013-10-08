package org.apache.streams.components.activityconsumer.impl;

import java.util.HashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.streams.components.activityconsumer.ActivityConsumerWarehouse;
import org.apache.streams.components.activityconsumer.ActivityConsumer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ActivityConsumerWarehouseImpl implements ActivityConsumerWarehouse {
    private static final transient Log LOG = LogFactory.getLog(ActivityConsumerWarehouseImpl.class);

    private HashMap<String,ActivityConsumer> consumers;

    public ActivityConsumerWarehouseImpl(){
        consumers = new HashMap<String, ActivityConsumer>();
    }

    public void register(ActivityConsumer activityConsumer) {
        //key in warehouse is the activity publisher UUID url parameter
        consumers.put(activityConsumer.getInRoute(), activityConsumer);
        activityConsumer.init();
    }

    public ActivityConsumer findConsumer(String id){
        return consumers.get(id);
    }


    public int getConsumersCount(){
        return consumers.size();
    }


}
