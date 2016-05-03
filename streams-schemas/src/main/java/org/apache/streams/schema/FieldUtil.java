package org.apache.streams.schema;

import com.fasterxml.jackson.databind.node.ObjectNode;

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
}
