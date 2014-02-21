package org.apache.streams.osgi.components.activityconsumer.impl;

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
