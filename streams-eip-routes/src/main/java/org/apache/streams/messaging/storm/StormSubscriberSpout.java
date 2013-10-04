package org.apache.streams.messaging.storm;

import backtype.storm.spout.SpoutOutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseRichSpout;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Values;
import backtype.storm.utils.Utils;
import org.apache.streams.osgi.components.activitysubscriber.ActivityStreamsSubscriberWarehouse;
import org.apache.streams.osgi.components.activitysubscriber.impl.ActivityStreamsSubscriberWarehouseImpl;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;

public class StormSubscriberSpout extends BaseRichSpout {
    private ActivityStreamsSubscriberWarehouse activityStreamsSubscriberWarehouse;
    private SpoutOutputCollector _collector;

    @Autowired
    public StormSubscriberSpout(ActivityStreamsSubscriberWarehouse activityStreamsSubscriberWarehouse){
          this.activityStreamsSubscriberWarehouse = activityStreamsSubscriberWarehouse;
    }

    @Override
    public void open(Map conf, TopologyContext context, SpoutOutputCollector collector) {
        _collector = collector;
    }

    @Override
    public void nextTuple() {
        Utils.sleep(10000);
        Values values =  new Values(activityStreamsSubscriberWarehouse.getAllSubscribers());
        _collector.emit(values);
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
        declarer.declare(new Fields("subscribers"));
    }
}
