package org.apache.streams.components.storm;

import backtype.storm.spout.SpoutOutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseRichSpout;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Values;
import backtype.storm.utils.Utils;
import org.apache.streams.components.activitysubscriber.ActivityStreamsSubscriber;
import org.apache.streams.components.activitysubscriber.ActivityStreamsSubscriberWarehouse;
import org.apache.streams.components.activitysubscriber.impl.ActivityStreamsSubscriberWarehouseImpl;
import org.apache.streams.components.service.StreamsActivityRepositoryService;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.xml.XmlBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.util.Iterator;
import java.util.Map;

@Component
public class StormSubscriberSpout extends BaseRichSpout {

    private static ApplicationContext appContext;
    private ActivityStreamsSubscriberWarehouse activityStreamsSubscriberWarehouse;
    private SpoutOutputCollector _collector;
    private Iterator iterator;

    @Autowired
    public StormSubscriberSpout(ApplicationContext ctx){
        appContext = ctx;
    }

    @Override
    public void open(Map conf, TopologyContext context, SpoutOutputCollector collector) {
        activityStreamsSubscriberWarehouse = (ActivityStreamsSubscriberWarehouse)appContext.getBean("activityStreamsSubscriberWarehouseImpl");

        _collector = collector;
    }

    @Override
    public void nextTuple() {
        Utils.sleep(10000);
        for (ActivityStreamsSubscriber activityStreamsSubscriber : activityStreamsSubscriberWarehouse.getAllSubscribers()) {
            _collector.emit(new Values(activityStreamsSubscriber));
        }
    }

    @Override
    public void ack(Object id) {
        System.out.println("RandomSentenceSpout.ack: "+ id);
    }

    @Override
    public void fail(Object id) {
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer) {
        declarer.declare(new Fields("subscriber"));
    }
}
