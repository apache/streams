package org.apache.streams.storm.trident;

/*
 * #%L
 * streams-runtime-storm
 * %%
 * Copyright (C) 2013 - 2014 Apache Streams Project
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import backtype.storm.task.TopologyContext;
import backtype.storm.tuple.Fields;
import com.google.common.collect.Lists;
import org.apache.commons.collections4.IteratorUtils;
import org.apache.streams.core.StreamsDatum;
import org.apache.streams.core.StreamsProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import storm.trident.operation.TridentCollector;
import storm.trident.spout.IBatchSpout;

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
        provider.prepare(topologyContext);
    }

    @Override
    public synchronized void emitBatch(long l, TridentCollector tridentCollector) {
        List<StreamsDatum> batch;
        batch = IteratorUtils.toList(provider.readCurrent().iterator());
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
        provider.cleanUp();
    }

    @Override
    public Map getComponentConfiguration() {
        return null;
    }

    @Override
    public Fields getOutputFields() {
        return new Fields("timestamp", "sequenceid", "document");
    }
};