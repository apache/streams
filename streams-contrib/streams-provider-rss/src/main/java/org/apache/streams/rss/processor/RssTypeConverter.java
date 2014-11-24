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

package org.apache.streams.rss.processor;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.Lists;
import org.apache.commons.lang.NotImplementedException;
import org.apache.streams.core.StreamsDatum;
import org.apache.streams.core.StreamsProcessor;
import org.apache.streams.pojo.json.Activity;
import org.apache.streams.rss.serializer.SyndEntryActivityConverter;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@Deprecated
/**
 * Converts ObjectNode representations of Rome SyndEntries to activities.
 * Deprecated: Modules and streams should adopt TypeConverterProcessor and ActivityConverterProcessor
 * TODO: Have RSS Provider always output ObjectNode, place ActivityConverterProcessor afterward with RssDocumentClassifier and RssConverterResolver available
 * TODO: Refactor tests and examples
 */
public class RssTypeConverter implements StreamsProcessor{

    private static final Logger LOGGER = LoggerFactory.getLogger(RssTypeConverter.class);

    private SyndEntryActivityConverter serializer;
    private int successCount = 0;
    private int failCount = 0;

    @Override
    public List<StreamsDatum> process(StreamsDatum datum) {
        List<StreamsDatum> datums = Lists.newLinkedList();
        if(datum.getDocument() instanceof ObjectNode) {
            Activity activity = this.serializer.deserialize((ObjectNode) datum.getDocument());
            datums.add(new StreamsDatum(activity, activity.getId(), DateTime.now().withZone(DateTimeZone.UTC)));
            successCount ++;
        } else {
            failCount ++;
            throw new NotImplementedException("Not implemented for class type : "+ datum.getDocument().getClass().toString());

        }
        LOGGER.debug("Processor current success count: {} and current fail: {}", successCount, failCount);

        return datums;
    }

    @Override
    public void prepare(Object o) {
        this.serializer = new SyndEntryActivityConverter();
    }

    @Override
    public void cleanUp() {

    }
}
