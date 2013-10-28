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
import org.apache.streams.components.service.StreamsSubscriptionRepositoryService;
import org.apache.streams.persistence.model.ActivityStreamsSubscription;
import org.apache.streams.persistence.repository.SubscriptionRepository;
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
    private StreamsSubscriptionRepositoryService repositoryService;
    private SpoutOutputCollector _collector;
    private Iterator iterator;

    @Autowired
    public StormSubscriberSpout(ApplicationContext ctx){
        appContext = ctx;
    }

    @Override
    public void open(Map conf, TopologyContext context, SpoutOutputCollector collector) {
        repositoryService = (StreamsSubscriptionRepositoryService)appContext.getBean("cassandraSubscriptionService");

        _collector = collector;
    }

    @Override
    public void nextTuple() {
        Utils.sleep(10000);
        for (ActivityStreamsSubscription subscription : repositoryService.getAllSubscriptions()) {
            _collector.emit(new Values(subscription));
        }
    }

    @Override
    public void ack(Object id) {
    }

    @Override
    public void fail(Object id) {
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer) {
        declarer.declare(new Fields("subscriber"));
    }
}
