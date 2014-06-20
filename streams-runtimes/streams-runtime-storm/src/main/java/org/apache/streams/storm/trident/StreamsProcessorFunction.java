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

package org.apache.streams.storm.trident;

import com.google.common.collect.Lists;
import org.apache.streams.core.StreamsDatum;
import org.apache.streams.core.StreamsPersistWriter;
import org.apache.streams.core.StreamsProcessor;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import storm.trident.operation.Function;
import storm.trident.operation.TridentCollector;
import storm.trident.operation.TridentOperationContext;
import storm.trident.tuple.TridentTuple;

import java.math.BigInteger;
import java.util.List;
import java.util.Map;

/**
 * Created by sblackmon on 4/6/14.
 */
public class StreamsProcessorFunction implements Function {

    private final static Logger LOGGER = LoggerFactory.getLogger(StreamsProcessorFunction.class);

    StreamsProcessor processor;

    @Override
    public void execute(TridentTuple objects, TridentCollector tridentCollector) {
        StreamsDatum datum = new StreamsDatum(
                objects.getValueByField("document"),
                new DateTime(objects.getLongByField("timestamp")),
                new BigInteger(objects.getStringByField("sequenceid"))
        );
        List<StreamsDatum> results = processor.process(datum);
        for( StreamsDatum result : results ) {
            tridentCollector.emit( Lists.newArrayList(
                    datum.getTimestamp(),
                    datum.getSequenceid(),
                    datum.getDocument()
            ));
        }
    }

    @Override
    public void prepare(Map map, TridentOperationContext tridentOperationContext) {
        processor.prepare(map);
    }

    @Override
    public void cleanup() {
        processor.cleanUp();
    }
}
