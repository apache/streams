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
import org.apache.streams.elasticsearch.ElasticsearchReaderConfiguration;
import org.apache.streams.jackson.StreamsJacksonMapper;

import java.io.IOException;
import java.io.Serializable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Uses index and type in metadata to populate current document into datums
 */
public class DocumentToMetadataProcessor implements StreamsProcessor, Serializable {

    public final static String STREAMS_ID = "DatumFromMetadataProcessor";

    private ElasticsearchClientManager elasticsearchClientManager;
    private ElasticsearchReaderConfiguration config;

    private ObjectMapper mapper;

    public DocumentToMetadataProcessor() {
        Config config = StreamsConfigurator.config.getConfig("elasticsearch");
        this.config = ElasticsearchConfigurator.detectReaderConfiguration(config);
    }

    public DocumentToMetadataProcessor(Config config) {
        this.config = ElasticsearchConfigurator.detectReaderConfiguration(config);
    }

    public DocumentToMetadataProcessor(ElasticsearchReaderConfiguration config) {
        this.config = config;
    }

    @Override
    public List<StreamsDatum> process(StreamsDatum entry) {
        List<StreamsDatum> result = Lists.newArrayList();

        ObjectNode metadataObjectNode;
        try {
            metadataObjectNode = mapper.readValue((String) entry.getDocument(), ObjectNode.class);
        } catch (IOException e) {
            return result;
        }

        Map<String, Object> metadata = asMap(metadataObjectNode);

        if(entry == null || metadata == null)
            return result;

        entry.setMetadata(metadata);

        result.add(entry);

        return result;
    }

    @Override
    public void prepare(Object configurationObject) {
        this.elasticsearchClientManager = new ElasticsearchClientManager(config);
        mapper = StreamsJacksonMapper.getInstance();
        mapper.registerModule(new JsonOrgModule());

    }

    @Override
    public void cleanUp() {
        this.elasticsearchClientManager.getClient().close();
    }

    public static Map<String, Object> asMap(JsonNode node) {

        Iterator<Map.Entry<String, JsonNode>> iterator = node.fields();
        Map<String, Object> ret = Maps.newHashMap();

        Map.Entry<String, JsonNode> entry;

        while (iterator.hasNext()) {
            entry = iterator.next();
            if( entry.getValue().asText() != null )
                ret.put(entry.getKey(), entry.getValue().asText());
        }

        return ret;
    }
}
