package org.apache.streams.util.schema;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * FieldUtil contains methods to assist in understanding fields defined within schemas.
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

}
