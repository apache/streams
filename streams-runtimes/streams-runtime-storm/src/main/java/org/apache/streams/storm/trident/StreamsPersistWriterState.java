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

import backtype.storm.task.IMetricsContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.streams.core.StreamsDatum;
import org.apache.streams.core.StreamsPersistWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import storm.trident.operation.TridentCollector;
import storm.trident.state.BaseStateUpdater;
import storm.trident.state.State;
import storm.trident.state.StateFactory;
import storm.trident.tuple.TridentTuple;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * Created by sblackmon on 1/16/14.
 */
public class StreamsPersistWriterState implements State {

    private final static Logger LOGGER = LoggerFactory.getLogger(StreamsPersistWriterState.class);

    StreamsPersistWriter writer;
    StreamsPersistStateController controller;

    public StreamsPersistWriterState(StreamsPersistStateController controller) {
        this.controller = new StreamsPersistStateController();
        writer.prepare(null);
    }

    public void bulkMessages(List<TridentTuple> tuples) {
        for (TridentTuple tuple : tuples) {
            StreamsDatum entry = this.controller.fromTuple(tuple);
            try {
                writer.write(entry);
            } catch (Exception e) {
                LOGGER.error("Exception writing entry : {}", e, entry);
            }
        }
        LOGGER.debug("******** Ending commit");
    }

    @Override
    public void beginCommit(Long aLong) {

    }

    @Override
    public void commit(Long aLong) {

    }

    public static class Factory implements StateFactory {

        private Logger logger;
        private StreamsPersistStateController controller;

        public Factory(StreamsPersistWriter writer, StreamsPersistStateController controller) {
            this.controller = controller;
            this.logger = LoggerFactory.getLogger(Factory.class);
        }

        @Override
        public State makeState(Map map, IMetricsContext iMetricsContext, int i, int i2) {
            this.logger.debug("Called makeState. . . ");
            // convert map to config object
            return new StreamsPersistWriterState(controller);
        }

    }

    public static class StreamsPersistStateController implements Serializable {

        private String fieldName;
        private ObjectMapper mapper = new ObjectMapper();

        public StreamsPersistStateController() {
            this.fieldName = "datum";
        }

        public StreamsPersistStateController(String fieldName) {
            this.fieldName = fieldName;
        }

        public StreamsDatum fromTuple(TridentTuple tuple) {
            return mapper.convertValue(tuple.getValueByField(this.fieldName), StreamsDatum.class);
        }

    }



    public static class StreamsPersistWriterSendMessage extends BaseStateUpdater<StreamsPersistWriterState> {

        private Logger logger = LoggerFactory.getLogger(StreamsPersistWriterSendMessage.class);

        @Override
        public void updateState(StreamsPersistWriterState writerState, List<TridentTuple> tridentTuples, TridentCollector tridentCollector) {
            this.logger.debug("****  calling send message. .  .");
            writerState.bulkMessages(tridentTuples);
        }
    }
}
