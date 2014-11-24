/*
Licensed to the Apache Software Foundation (ASF) under one
or more contributor license agreements.  See the NOTICE file
distributed with this work for additional information
regarding copyright ownership.  The ASF licenses this file
to you under the Apache License, Version 2.0 (the
"License"); you may not use this file except in compliance
with the License.  You may obtain a copy of the License at

  http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing,
software distributed under the License is distributed on an
"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
KIND, either express or implied.  See the License for the
specific language governing permissions and limitations
under the License.
*/
package org.apache.streams.datasift.processor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import org.apache.streams.core.StreamsDatum;
import org.apache.streams.core.StreamsProcessor;
import org.apache.streams.datasift.Datasift;
import org.apache.streams.datasift.serializer.DatasiftActivityConverter;
import org.apache.streams.datasift.util.StreamsDatasiftMapper;
import org.apache.streams.jackson.StreamsJacksonMapper;
import org.apache.streams.pojo.json.Activity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 *
 */
public class DatasiftActivitySerializerProcessor implements StreamsProcessor {

    private final static Logger LOGGER = LoggerFactory.getLogger(DatasiftActivitySerializerProcessor.class);

    private ObjectMapper mapper;
    private Class outClass;
    private DatasiftActivityConverter datasiftActivitySerializer;

    public final static String TERMINATE = new String("TERMINATE");

    public DatasiftActivitySerializerProcessor(Class outClass) {
        this.outClass = outClass;
    }

    @Override
    public List<StreamsDatum> process(StreamsDatum entry) {
        List<StreamsDatum> result = Lists.newLinkedList();
        Activity activity;
        try {
            Datasift node;
            if( entry.getDocument() instanceof String ) {
                node = this.mapper.readValue((String)entry.getDocument(), Datasift.class);
            } else if( entry.getDocument() instanceof Datasift ) {
                node = (Datasift) entry.getDocument();
            } else {
                node = this.mapper.convertValue(entry.getDocument(), Datasift.class);
            }
            if(node != null) {
                activity = this.datasiftActivitySerializer.deserialize(node);
                StreamsDatum datum = new StreamsDatum(activity, entry.getId(), entry.getTimestamp(), entry.getSequenceid());
                datum.setMetadata(entry.getMetadata());
                result.add(datum);
            }
        } catch (Exception e) {
            LOGGER.error("Exception converting Datasift Interaction to "+this.outClass.getName()+ " : {}", e);
        }
        return result;
    }

    @Override
    public void prepare(Object configurationObject) {
        this.mapper = StreamsJacksonMapper.getInstance(StreamsDatasiftMapper.DATASIFT_FORMAT);
        this.datasiftActivitySerializer = new DatasiftActivityConverter();
    }

    @Override
    public void cleanUp() {

    }

};
