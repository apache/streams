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
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.Lists;
import org.apache.streams.core.StreamsDatum;
import org.apache.streams.core.StreamsProcessor;
import org.apache.streams.datasift.Datasift;
import org.apache.streams.datasift.provider.DatasiftConverter;
import org.apache.streams.datasift.serializer.DatasiftActivitySerializer;
import org.apache.streams.datasift.util.StreamsDatasiftMapper;
import org.apache.streams.pojo.json.Activity;
import org.apache.streams.jackson.CleanAdditionalPropertiesProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 *
 */
public class DatasiftTypeConverterProcessor implements StreamsProcessor {

    private final static Logger LOGGER = LoggerFactory.getLogger(DatasiftTypeConverterProcessor.class);

    private ObjectMapper mapper;
    private Class outClass;
    private DatasiftActivitySerializer datasiftInteractionActivitySerializer;
    private DatasiftConverter converter;

    public final static String TERMINATE = new String("TERMINATE");

    public DatasiftTypeConverterProcessor(Class outClass) {
        this.outClass = outClass;
    }

    @Override
    public List<StreamsDatum> process(StreamsDatum entry) {
        List<StreamsDatum> result = Lists.newLinkedList();
        Object doc;
        try {
            doc = this.converter.convert(entry.getDocument(), this.mapper);
            if(doc != null) {
                result.add(new StreamsDatum(doc, entry.getId()));
            }
        } catch (Exception e) {
            LOGGER.error("Exception converting Datasift Interaction to "+this.outClass.getName()+ " : {}", e);
        }
        return result;
    }

    @Override
    public void prepare(Object configurationObject) {
        this.mapper = StreamsDatasiftMapper.getInstance();
        this.datasiftInteractionActivitySerializer = new DatasiftActivitySerializer();
        if(this.outClass.equals(Activity.class)) {
            this.converter = new ActivityConverter();
        } else if (this.outClass.equals(String.class)) {
            this.converter = new StringConverter();
        } else {
            LOGGER.warn("Using defaulting datasift converter");
            this.converter = new DefaultConverter(this.outClass);
        }
    }

    @Override
    public void cleanUp() {

    }

    private class ActivityConverter implements DatasiftConverter {

        @Override
        public Object convert(Object toConvert, ObjectMapper mapper) {
            if(toConvert instanceof Activity)
                return toConvert;
            try {
                if(toConvert instanceof String)
                    return datasiftInteractionActivitySerializer.deserialize((String) toConvert);
                return mapper.convertValue(toConvert, Activity.class);
            } catch (Exception e) {
                LOGGER.error("Exception while trying to convert {} to a Activity.", toConvert.getClass());
                LOGGER.error("Exception : {}", e);
                e.printStackTrace();
                return null;
            }
        }


    }

    private class StringConverter implements DatasiftConverter {
        @Override
        public Object convert(Object toConvert, ObjectMapper mapper) {
            try {
                if(toConvert instanceof String){
                    return mapper.writeValueAsString(mapper.readValue((String) toConvert, Datasift.class));
                } else {
                    if(toConvert.getClass().equals(Activity.class)) { //hack to remove additional properties
                        ObjectNode node = mapper.convertValue(toConvert, ObjectNode.class);
                        CleanAdditionalPropertiesProcessor.cleanAdditionalProperties(node);
                        return mapper.writeValueAsString(node);
                    } else
                        return mapper.writeValueAsString(toConvert);
                }
            } catch (Exception e) {
                LOGGER.error("Exception while trying to write {} as a String.", toConvert.getClass());
                LOGGER.error("Exception : {}", e);
                return null;
            }
        }
    }

    private class DefaultConverter implements DatasiftConverter {

        private Class clazz;

        public DefaultConverter(Class clazz) {
            this.clazz = clazz;
        }

        @Override
        public Object convert(Object toConvert, ObjectMapper mapper) {
            try {
                if(toConvert instanceof String) {
                    return mapper.readValue((String) toConvert, this.clazz);
                } else {
                    return mapper.convertValue(toConvert, this.clazz);
                }

            } catch (Exception e) {
                throw new RuntimeException("Failed converting +"+ toConvert.getClass().getName()+" to "+ this.clazz.getName());
            }
        }
    }
};
