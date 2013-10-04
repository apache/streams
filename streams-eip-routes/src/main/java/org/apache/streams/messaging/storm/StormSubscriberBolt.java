package org.apache.streams.messaging.storm;

import backtype.storm.coordination.BatchOutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.BasicOutputCollector;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseBasicBolt;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Tuple;
import org.apache.streams.messaging.service.ActivityService;
import org.apache.streams.osgi.components.activitysubscriber.ActivityStreamsSubscriber;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;

public class StormSubscriberBolt extends BaseBasicBolt {
    BatchOutputCollector _collector;
    ActivityService activityService;

    @Autowired
    public StormSubscriberBolt(ActivityService activityService){
        this.activityService = activityService;
    }

    @Override
    public void prepare(Map stormConf, TopologyContext context){
        super.prepare(stormConf, context);
    }

    @Override
    public void execute(Tuple tuple, BasicOutputCollector collector) {
        List<Object> subscribers = tuple.getValues();
        for(Object subscriber:subscribers){
            if(subscriber instanceof ActivityStreamsSubscriber){
                this.updateSubscriber((ActivityStreamsSubscriber)subscriber);
            }
        }
    }

    public void updateSubscriber(ActivityStreamsSubscriber subscriber){
        Set<String> activities = new TreeSet<String>();
        activities.addAll(activityService.getActivitiesForFilters(subscriber.getActivityStreamsSubscriberConfiguration().getFilters(), subscriber.getLastUpdated()));
        //TODO: an activity posted in between the cql query and setting the lastUpdated field will be lost
        subscriber.setLastUpdated(new Date());
        subscriber.receive(new ArrayList<String>(activities));
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer) {
        declarer.declare(new Fields("void"));
    }
}
