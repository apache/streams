package org.apache.streams.components.storm;

import backtype.storm.coordination.BatchOutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.BasicOutputCollector;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseBasicBolt;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Tuple;
import org.apache.streams.components.service.StreamsActivityRepositoryService;
import org.apache.streams.components.activitysubscriber.ActivityStreamsSubscriber;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class StormSubscriberBolt extends BaseBasicBolt {
    private static ApplicationContext appContext;
    BatchOutputCollector _collector;
    StreamsActivityRepositoryService activityService;

    @Autowired
    public StormSubscriberBolt(ApplicationContext ctx) {
        appContext = ctx;
    }

    @Override
    public void prepare(Map stormConf, TopologyContext context) {
        activityService = (StreamsActivityRepositoryService) appContext.getBean("cassandraActivityService");
        super.prepare(stormConf, context);
    }

    @Override
    public void execute(Tuple tuple, BasicOutputCollector collector) {
        if(tuple.getValue(0) instanceof ActivityStreamsSubscriber){
            ActivityStreamsSubscriber subscriber = (ActivityStreamsSubscriber) tuple.getValue(0);
            this.updateSubscriber(subscriber);
        }
    }

    public void updateSubscriber(ActivityStreamsSubscriber subscriber) {
        Set<String> activities = new TreeSet<String>();
        activities.addAll(activityService.getActivitiesForFilters(subscriber.getSubscription().getFilters(), subscriber.getLastUpdated()));
        //TODO: an activity posted in between the cql query and setting the lastUpdated field will be lost
        subscriber.setLastUpdated(new Date());
        subscriber.receive(new ArrayList<String>(activities));
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer) {
        declarer.declare(new Fields("void"));
    }
}
