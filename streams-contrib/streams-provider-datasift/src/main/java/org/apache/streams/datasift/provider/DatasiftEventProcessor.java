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
package org.apache.streams.datasift.provider;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import org.apache.commons.lang.NotImplementedException;
import org.apache.streams.core.StreamsDatum;
import org.apache.streams.core.StreamsProcessor;
import org.apache.streams.datasift.Datasift;
import org.apache.streams.datasift.serializer.DatasiftActivitySerializer;
import org.apache.streams.jackson.StreamsJacksonMapper;
import org.apache.streams.pojo.json.Activity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Created by sblackmon on 12/10/13.
 */
public class DatasiftEventProcessor implements StreamsProcessor {

    private final static Logger LOGGER = LoggerFactory.getLogger(DatasiftEventProcessor.class);

    private ObjectMapper mapper;
    private Class outClass;
    private DatasiftActivitySerializer datasiftInteractionActivitySerializer;
    private DatasiftTypeConverter converter;

    public final static String TERMINATE = new String("TERMINATE");

    public DatasiftEventProcessor(Class outClass) {
        this.outClass = outClass;
    }

    @Override
    public List<StreamsDatum> process(StreamsDatum entry) {
        List<StreamsDatum> result = Lists.newLinkedList();
        Object item = entry.getDocument();
        try {
            Datasift datasift = mapper.convertValue(item, Datasift.class);
            result.add(this.converter.convert(datasift));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    @Override
    public void prepare(Object configurationObject) {
        this.mapper = new StreamsJacksonMapper();
        this.datasiftInteractionActivitySerializer = new DatasiftActivitySerializer();
        if(this.outClass.equals(Activity.class)) {
            this.converter = new ActivityConverter();
        } else if (this.outClass.equals(String.class)) {
            this.converter = new StringConverter();
        } else {
            throw new NotImplementedException("No converter implemented for class : "+this.outClass.getName());
        }
    }

    @Override
    public void cleanUp() {

    }

    private class ActivityConverter implements DatasiftTypeConverter {
        @Override
        public StreamsDatum convert(Datasift datasift) {
            return new StreamsDatum(datasiftInteractionActivitySerializer.deserialize(datasift), datasift.getInteraction().getId());
        }
    }

    private class StringConverter implements DatasiftTypeConverter {
        @Override
        public StreamsDatum convert(Datasift datasift) {
            try {
                return new StreamsDatum(mapper.writeValueAsString(datasift), datasift.getInteraction().getId());
            } catch (JsonProcessingException jpe) {
                throw new RuntimeException(jpe);
            }
        }
    }
};
