package org.apache.streams.elasticsearch;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.Maps;
import org.apache.streams.core.StreamsDatum;

import java.util.Iterator;
import java.util.Map;

/**
 * Created by sblackmon on 10/20/14.
 */
public class ElasticsearchMetadataUtil {

    public static String getIndex(Map<String, Object> metadata, ElasticsearchWriterConfiguration config) {

        String index = null;

        if( metadata != null && metadata.containsKey("index"))
            index = (String) metadata.get("index");

        if(index == null || (config.getForceUseConfig() != null && config.getForceUseConfig())) {
            index = config.getIndex();
        }

        return index;
    }

    public static String getType(Map<String, Object> metadata, ElasticsearchWriterConfiguration config) {

        String type = null;

        if( metadata != null && metadata.containsKey("type"))
            type = (String) metadata.get("type");

        if(type == null || (config.getForceUseConfig() != null && config.getForceUseConfig())) {
            type = config.getType();
        }


        return type;
    }

    public static String getIndex(Map<String, Object> metadata, ElasticsearchReaderConfiguration config) {

        String index = null;

        if( metadata != null && metadata.containsKey("index"))
            index = (String) metadata.get("index");

        if(index == null) {
            index = config.getIndexes().get(0);
        }

        return index;
    }

    public static String getType(Map<String, Object> metadata, ElasticsearchReaderConfiguration config) {

        String type = null;

        if( metadata != null && metadata.containsKey("type"))
            type = (String) metadata.get("type");

        if(type == null) {
            type = config.getTypes().get(0);
        }


        return type;
    }

    public static String getId(StreamsDatum datum) {

        String id = datum.getId();

        Map<String, Object> metadata = datum.getMetadata();

        if( id == null && metadata != null && metadata.containsKey("id"))
            id = (String) datum.getMetadata().get("id");

        return id;
    }

    public static String getId(Map<String, Object> metadata) {

        return (String) metadata.get("id");

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
