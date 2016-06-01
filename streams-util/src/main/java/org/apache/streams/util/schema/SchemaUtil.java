package org.apache.streams.util.schema;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.Iterator;
import java.util.Map;

import static org.apache.commons.lang3.StringUtils.isEmpty;

/**
 * SchemaUtil contains methods to assist in resolving schemas and schema fragments.
 */
public class SchemaUtil {

    private final static Logger LOGGER = LoggerFactory.getLogger(SchemaUtil.class);
    private static final JsonNodeFactory NODE_FACTORY = JsonNodeFactory.instance;
    public static final String ILLEGAL_CHARACTER_REGEX = "[^0-9a-zA-Z_$]";

    public static String childQualifiedName(String parentQualifiedName, String childSimpleName) {
        String safeChildName = childSimpleName.replaceAll(ILLEGAL_CHARACTER_REGEX, "_");
        return isEmpty(parentQualifiedName) ? safeChildName : parentQualifiedName + "." + safeChildName;
    }

    public static ObjectNode readSchema(URL schemaUrl) {

        ObjectNode schemaNode = NODE_FACTORY.objectNode();
        schemaNode.put("$ref", schemaUrl.toString());
        return schemaNode;

    }

    public static ObjectNode mergeProperties(ObjectNode content, ObjectNode parent) {

        ObjectNode merged = parent.deepCopy();
        Iterator<Map.Entry<String, JsonNode>> fields = content.fields();
        for( ; fields.hasNext(); ) {
            Map.Entry<String, JsonNode> field = fields.next();
            String fieldId = field.getKey();
            merged.put(fieldId, field.getValue().deepCopy());
        }
        return merged;
    }

}
