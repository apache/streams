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
import org.elasticsearch.action.get.GetRequestBuilder;
import org.elasticsearch.action.get.GetResponse;
import org.joda.time.DateTime;

import java.io.IOException;
import java.io.Serializable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by sblackmon on 9/4/14.
 */
public class DatumFromMetadataAsDocumentProcessor implements StreamsProcessor, Serializable {

    public final static String STREAMS_ID = "DatumFromMetadataProcessor";

    private ElasticsearchClientManager elasticsearchClientManager;
    private ElasticsearchReaderConfiguration config;

    private ObjectMapper mapper;

    public DatumFromMetadataAsDocumentProcessor() {
        Config config = StreamsConfigurator.config.getConfig("elasticsearch");
        this.config = ElasticsearchConfigurator.detectReaderConfiguration(config);
    }

    public DatumFromMetadataAsDocumentProcessor(Config config) {
        this.config = ElasticsearchConfigurator.detectReaderConfiguration(config);
    }

    public DatumFromMetadataAsDocumentProcessor(ElasticsearchReaderConfiguration config) {
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

        if(entry == null || entry.getMetadata() == null)
            return result;

        String index = (String) metadata.get("index");
        String type = (String) metadata.get("type");
        String id = (String) metadata.get("id");

        if( index == null ) {
            index = this.config.getIndexes().get(0);
        }
        if( type == null ) {
            type = this.config.getTypes().get(0);
        }
        if( id == null ) {
            id = entry.getId();
        }

        GetRequestBuilder getRequestBuilder = elasticsearchClientManager.getClient().prepareGet(index, type, id);
        getRequestBuilder.setFields("*", "_timestamp");
        getRequestBuilder.setFetchSource(true);
        GetResponse getResponse = getRequestBuilder.get();

        if( getResponse == null || getResponse.isExists() == false || getResponse.isSourceEmpty() == true )
            return result;

        entry.setDocument(getResponse.getSource());
        if( getResponse.getField("_timestamp") != null) {
            DateTime timestamp = new DateTime(((Long) getResponse.getField("_timestamp").getValue()).longValue());
            entry.setTimestamp(timestamp);
        }

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

    public Map<String, Object> asMap(JsonNode node) {

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
