package org.apache.streams.components.storm;

import backtype.storm.coordination.BatchOutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.BasicOutputCollector;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseBasicBolt;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Tuple;
import org.apache.streams.components.activitysubscriber.ActivityStreamsSubscriberWarehouse;
import org.apache.streams.persistence.model.ActivityStreamsSubscription;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class StormSubscriberBolt extends BaseBasicBolt {
    private static ApplicationContext appContext;
    BatchOutputCollector _collector;
    private ActivityStreamsSubscriberWarehouse subscriberWarehouse;

    @Autowired
    public StormSubscriberBolt(ApplicationContext ctx) {
        appContext = ctx;
    }

    @Override
    public void prepare(Map stormConf, TopologyContext context) {
        subscriberWarehouse = (ActivityStreamsSubscriberWarehouse) appContext.getBean("activityStreamsSubscriberWarehouseImpl");
        super.prepare(stormConf, context);
    }

    @Override
    public void execute(Tuple tuple, BasicOutputCollector collector) {
        if(tuple.getValue(0) instanceof ActivityStreamsSubscription){
            ActivityStreamsSubscription subscription = (ActivityStreamsSubscription) tuple.getValue(0);
            if(subscriberWarehouse.getSubscriber(subscription.getInRoute()) == null){
                subscriberWarehouse.register(subscription);
            }
            subscriberWarehouse.updateSubscriber(subscription);
        }
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer) {
        declarer.declare(new Fields("void"));
    }
}
