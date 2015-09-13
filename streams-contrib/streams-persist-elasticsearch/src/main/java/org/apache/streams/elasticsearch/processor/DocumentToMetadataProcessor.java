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

package org.apache.streams.elasticsearch.processor;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.datatype.jsonorg.JsonOrgModule;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.typesafe.config.Config;
import org.apache.streams.config.StreamsConfigurator;
import org.apache.streams.core.StreamsDatum;
import org.apache.streams.core.StreamsProcessor;
import org.apache.streams.elasticsearch.ElasticsearchClientManager;
import org.apache.streams.elasticsearch.ElasticsearchConfigurator;
import org.apache.streams.elasticsearch.ElasticsearchMetadataUtil;
import org.apache.streams.elasticsearch.ElasticsearchReaderConfiguration;
import org.apache.streams.jackson.StreamsJacksonMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.Serializable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Moves a json representation of metadata out of the document to the metadata field.
 *
 * This is useful if you have a list of document metadata references in the document field,
 * for example loaded from a file, and need them in the metadata field.
 */
public class DocumentToMetadataProcessor implements StreamsProcessor, Serializable {

    public final static String STREAMS_ID = "DatumFromMetadataProcessor";

    private ObjectMapper mapper;

    private static final Logger LOGGER = LoggerFactory.getLogger(DocumentToMetadataProcessor.class);

    public DocumentToMetadataProcessor() {
    }

    @Override
    public String getId() {
        return STREAMS_ID;
    }

    @Override
    public List<StreamsDatum> process(StreamsDatum entry) {
        List<StreamsDatum> result = Lists.newArrayList();

        Object object = entry.getDocument();
        ObjectNode metadataObjectNode;
        try {
            String docAsJson = (object instanceof String) ? object.toString() : mapper.writeValueAsString(object);
            metadataObjectNode = mapper.readValue(docAsJson, ObjectNode.class);
        } catch (Throwable e) {
            LOGGER.warn("Exception: %s", e.getMessage());
            return result;
        }

        Map<String, Object> metadata = ElasticsearchMetadataUtil.asMap(metadataObjectNode);

        if(entry == null || metadata == null)
            return result;

        entry.setMetadata(metadata);

        result.add(entry);

        return result;
    }

    @Override
    public void prepare(Object configurationObject) {
        mapper = StreamsJacksonMapper.getInstance();
        mapper.registerModule(new JsonOrgModule());
    }

    @Override
    public void cleanUp() {
        mapper = null;
    }

}
