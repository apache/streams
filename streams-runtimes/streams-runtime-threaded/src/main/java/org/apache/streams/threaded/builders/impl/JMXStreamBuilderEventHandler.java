/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.streams.threaded.builders.impl;

import com.google.common.collect.Maps;
import org.apache.streams.monitoring.tasks.LocalRuntimeBroadcastMonitorThread;
import org.apache.streams.threaded.builders.StreamBuilderEventHandler;
import org.apache.streams.threaded.builders.StreamsGraphElement;
import org.apache.streams.threaded.builders.ThreadedStreamBuilder;
import org.apache.streams.threaded.counters.DatumStatusCounter;
import org.apache.streams.threaded.tasks.StatusCounts;
import org.slf4j.Logger;

import java.util.List;
import java.util.Map;

public class JMXStreamBuilderEventHandler extends LocalRuntimeBroadcastMonitorThread implements StreamBuilderEventHandler {
    private static final Logger LOGGER = org.slf4j.LoggerFactory.getLogger(JMXStreamBuilderEventHandler.class);

    private static Map<String, StatusCounts> graph;
    private Map<String, DatumStatusCounter> counterBeans;
    private String streamName;

    public JMXStreamBuilderEventHandler(Map<String, Object> streamConfig) {
        super(streamConfig);

        setStreamName(streamConfig);
        this.graph = null;
        this.counterBeans = Maps.newHashMap();
    }

    /**
     * Calculate what the Stream Name is going to be for these JMX Beans
     * @param streamConfig
     */
    private void setStreamName(Map<String, Object> streamConfig) {
        if(streamConfig != null && streamConfig.containsKey(ThreadedStreamBuilder.STREAM_IDENTIFIER_KEY)
                && streamConfig.get(ThreadedStreamBuilder.STREAM_IDENTIFIER_KEY) != null
                && !streamConfig.get(ThreadedStreamBuilder.STREAM_IDENTIFIER_KEY).equals("")) {
            this.streamName = streamConfig.get(ThreadedStreamBuilder.STREAM_IDENTIFIER_KEY).toString();
        } else {
            this.streamName = ThreadedStreamBuilder.DEFAULT_STREAM_IDENTIFIER;
        }
    }

    /**
     * Given a map of IDs -> StatusCounts objects, instantiate the appropriate
     * DatumStatusCounter Objects. This will also instantiate and initialize all
     * JMX Beans
     * @param graph
     */
    public void setGraph(Map<String, StatusCounts> graph) {
        this.graph = graph;

        try {
            for(Map.Entry<String, StatusCounts> element : graph.entrySet()) {
                DatumStatusCounter counter = new DatumStatusCounter(element.getValue().getId(), this.streamName, System.currentTimeMillis());

                counterBeans.put(element.getValue().getId(), counter);
            }
        } catch (Exception e) {
            LOGGER.error("Exception while attempting to create relevant MBeans: {}", e);
        }
    }

    /**
     * Given an updated map of all counts, ensure that the JMX beans reflect the latest
     * data
     * @param counts
     */
    private void updateBeans(Map<String, StatusCounts> counts) {
        for(Map.Entry<String, DatumStatusCounter> entry : this.counterBeans.entrySet()) {
            StatusCounts count = counts.get(entry.getKey());

            DatumStatusCounter currentBean = entry.getValue();

            //Set the successful count
            long delta = count.getSuccess() - currentBean.getNumPassed();
            currentBean.incrementPassedCount(delta);

            //Set the failed count
            delta = count.getFailed() - currentBean.getNumFailed();
            currentBean.incrementFailedCount(delta);
        }
    }

    @Override
    public void update(Map<String, StatusCounts> counts, List<StreamsGraphElement> graph) {
        if(this.graph != null) {
            updateBeans(counts);
            persistMessages();
        } else if(graph != null) {
            setGraph(counts);
        }
    }
}