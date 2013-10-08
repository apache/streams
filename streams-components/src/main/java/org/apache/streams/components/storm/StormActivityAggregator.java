package org.apache.streams.components.storm;

import backtype.storm.Config;
import backtype.storm.LocalCluster;
import backtype.storm.topology.TopologyBuilder;

public class StormActivityAggregator {

    private StormSubscriberBolt bolt;
    private StormSubscriberSpout spout;

    public StormActivityAggregator(StormSubscriberSpout spout, StormSubscriberBolt bolt){
        this.bolt = bolt;
        this.spout = spout;
    }

    //@PostConstruct
    public void aggregate() {
        TopologyBuilder builder = new TopologyBuilder();

        builder.setBolt("distribute", bolt, 3).shuffleGrouping("activity");
        builder.setSpout("activity", spout, 10);

        Config conf = new Config();
        conf.setDebug(true);

        LocalCluster cluster = new LocalCluster();
        cluster.submitTopology("test", conf, builder.createTopology());
    }
}
