package org.apache.streams.storm.trident;

import backtype.storm.task.TopologyContext;
import backtype.storm.tuple.Fields;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import org.apache.commons.collections4.IteratorUtils;
import org.apache.streams.core.StreamsDatum;
import org.apache.streams.core.StreamsProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import storm.trident.operation.TridentCollector;
import storm.trident.spout.IBatchSpout;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * Created by sblackmon on 1/16/14.
 */
public class StreamsProviderSpout implements IBatchSpout {

    private final static Logger LOGGER = LoggerFactory.getLogger(StreamsProviderSpout.class);

    StreamsProvider provider;

    public StreamsProviderSpout(StreamsProvider provider) {
        this.provider = provider;
    }

    @Override
    public void open(Map map, TopologyContext topologyContext) {
        provider.start();
    }

    @Override
    public synchronized void emitBatch(long l, TridentCollector tridentCollector) {
        List<StreamsDatum> batch;
        batch = IteratorUtils.toList(provider.getProviderQueue().iterator());
        for( StreamsDatum datum : batch ) {
            tridentCollector.emit( Lists.newArrayList(
                    datum.getTimestamp(),
                    datum.getSequenceid(),
                    datum.getDocument()
            ));
        }
    }

    @Override
    public void ack(long l) {

    }

    @Override
    public void close() {
        provider.stop();
    }

    @Override
    public Map getComponentConfiguration() {
        return null;
    }

    @Override
    public Fields getOutputFields() {
        return new Fields("timestamp", "sequenceid", "datum");
    }
};