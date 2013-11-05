package org.apache.streams.components.storm;

import backtype.storm.Config;
import backtype.storm.LocalCluster;
import backtype.storm.topology.TopologyBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component
public class StormActivityAggregator {

    private StormSubscriberBolt bolt;
    private StormSubscriberSpout spout;

    @Autowired
    public StormActivityAggregator(StormSubscriberSpout spout, StormSubscriberBolt bolt){
        this.spout = spout;
        this.bolt = bolt;
    }

    @PostConstruct
    public void aggregate() {

        TopologyBuilder builder = new TopologyBuilder();

        builder.setSpout("activity", spout, 10);
        builder.setBolt("distribute", bolt, 3).shuffleGrouping("activity");

        Config conf = new Config();
        conf.setDebug(false);

        LocalCluster cluster = new LocalCluster();
        cluster.submitTopology("test", conf, builder.createTopology());

    }
}
