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

package org.apache.streams.graph;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.Lists;
import org.apache.streams.components.http.HttpProviderConfiguration;
import org.apache.streams.components.http.provider.SimpleHttpProvider;
import org.apache.streams.config.ComponentConfigurator;
import org.apache.streams.config.StreamsConfigurator;
import org.apache.streams.core.StreamsPersistReader;
import org.apache.streams.core.StreamsResultSet;
import org.apache.streams.data.util.PropertyUtil;
import org.apache.streams.graph.neo4j.CypherQueryResponse;
import org.apache.streams.graph.neo4j.ItemData;
import org.apache.streams.graph.neo4j.ItemMetadata;
import org.apache.streams.jackson.StreamsJacksonMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Reads a stream of activityobjects from vertices in a graph database with
 * an http rest endpoint (such as neo4j)
 */
public class GraphVertexReader extends SimpleHttpProvider implements StreamsPersistReader {

    public static final String STREAMS_ID = GraphVertexReader.class.getCanonicalName();

    private final static Logger LOGGER = LoggerFactory.getLogger(GraphVertexReader.class);

    protected GraphReaderConfiguration configuration;

    private static ObjectMapper mapper = StreamsJacksonMapper.getInstance();

    public GraphVertexReader() {
        this(new ComponentConfigurator<GraphReaderConfiguration>(GraphReaderConfiguration.class).detectConfiguration(StreamsConfigurator.config.getConfig("graph")));
    }

    public GraphVertexReader(GraphReaderConfiguration configuration) {
        super(mapper.convertValue(configuration, HttpProviderConfiguration.class));
        if( configuration.getType().equals(GraphHttpConfiguration.Type.NEO_4_J))
            super.configuration.setResourcePath("/db/" + configuration.getGraph() + "/transaction/commit");
        else if( configuration.getType().equals(GraphHttpConfiguration.Type.REXSTER))
            super.configuration.setResourcePath("/graphs/" + configuration.getGraph());
        this.configuration = configuration;
    }

    /*
     * Neo API query returns something like this:
     * { "columns": [ "v" ], "data": [ [ { "data": { props }, etc... } ], [ { "data": { props }, etc... } ] ] }
     *
     */
    public List<ObjectNode> parse(JsonNode jsonNode) {
        List<ObjectNode> results = Lists.newArrayList();

        ObjectNode root = (ObjectNode) jsonNode;

        CypherQueryResponse cypherQueryResponse = mapper.convertValue(root, CypherQueryResponse.class);

        for( List<List<ItemMetadata>> dataWrapper : cypherQueryResponse.getData()) {

            for (List<ItemMetadata> itemMetadatas : dataWrapper) {

                for (ItemMetadata itemMetadata : itemMetadatas) {

                    ItemData itemData = itemMetadata.getData();

                    LOGGER.debug("itemData: " + itemData);

                    results.add(PropertyUtil.unflattenMap(itemData.getAdditionalProperties(), '.'));
                }

            }

        }
        return results;
    }

    @Override
    public String getId() {
        return STREAMS_ID;
    }

    @Override
    public void prepare(Object configurationObject) {

        super.prepare(configurationObject);

    }

    @Override
    public StreamsResultSet readAll() {
        return readCurrent();
    }
}
