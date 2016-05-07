package org.apache.streams.schema;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by steve on 5/1/16.
 */
public class FieldUtil {

    public static FieldType determineFieldType(ObjectNode fieldNode) {
        String typeSchemaField = "type";
        if( !fieldNode.has(typeSchemaField))
            return null;
        String typeSchemaFieldValue = fieldNode.get(typeSchemaField).asText();
        if( typeSchemaFieldValue.equals("string")) {
            return FieldType.STRING;
        } else if( typeSchemaFieldValue.equals("integer")) {
            return FieldType.INTEGER;
        } else if( typeSchemaFieldValue.equals("number")) {
            return FieldType.NUMBER;
        } else if( typeSchemaFieldValue.equals("object")) {
            return FieldType.OBJECT;
        } else if( typeSchemaFieldValue.equals("boolean")) {
            return FieldType.BOOLEAN;
        } else if( typeSchemaFieldValue.equals("array")) {
            return FieldType.ARRAY;
        }
        else return null;
    }

    public static FieldType determineArrayType(ObjectNode fieldNode) {
        if( fieldNode == null ) return null;
        ObjectNode itemsObjectNode = resolveItemsNode(fieldNode);
        if( itemsObjectNode != null)
            return determineFieldType(itemsObjectNode);
        return null;
    }

    public static ObjectNode resolveItemsNode(ObjectNode fieldNode) {
        ObjectNode itemsObjectNode = null;

        if( fieldNode.get("items").isObject() )
            itemsObjectNode = (ObjectNode) fieldNode.get("items");
        else if( fieldNode.get("items").isArray() && fieldNode.size() > 0 && fieldNode.get(0).isObject()) {
            itemsObjectNode = (ObjectNode) fieldNode.get("items").get(0);
        }

        return itemsObjectNode;
    }

}
