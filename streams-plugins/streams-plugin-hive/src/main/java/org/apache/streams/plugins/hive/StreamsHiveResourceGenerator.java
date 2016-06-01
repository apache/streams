package org.apache.streams.plugins.hive;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import org.apache.streams.util.schema.FieldType;
import org.apache.streams.util.schema.FieldUtil;
import org.apache.streams.util.schema.FileUtil;
import org.apache.streams.util.schema.GenerationConfig;
import org.apache.streams.util.schema.Schema;
import org.apache.streams.util.schema.SchemaStore;
import org.apache.streams.util.schema.SchemaStoreImpl;
import org.jsonschema2pojo.util.URLUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.URL;
import java.util.*;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.apache.commons.lang3.StringUtils.defaultString;
import static org.apache.streams.util.schema.FileUtil.*;

/**
 * Generates hive table definitions for using org.openx.data.jsonserde.JsonSerDe on new-line delimited json documents.
 *
 *
 */
public class StreamsHiveResourceGenerator implements Runnable {

    private final static Logger LOGGER = LoggerFactory.getLogger(StreamsHiveResourceGenerator.class);

    private final static String LS = System.getProperty("line.separator");

    private StreamsHiveGenerationConfig config;

    private SchemaStore schemaStore = new SchemaStoreImpl();

    private int currentDepth = 0;

    public static void main(String[] args) {
        StreamsHiveGenerationConfig config = new StreamsHiveGenerationConfig();

        String sourceDirectory = "src/main/jsonschema";
        String targetDirectory = "target/generated-resources/hive";

        if( args.length > 0 )
            sourceDirectory = args[0];
        if( args.length > 1 )
            targetDirectory = args[1];

        config.setSourceDirectory(sourceDirectory);
        config.setTargetDirectory(targetDirectory);

        StreamsHiveResourceGenerator streamsHiveResourceGenerator = new StreamsHiveResourceGenerator(config);
        streamsHiveResourceGenerator.run();
    }

    public StreamsHiveResourceGenerator(StreamsHiveGenerationConfig config) {
        this.config = config;
    }

    public void run() {

        checkNotNull(config);

        generate(config);

    }

    public void generate(StreamsHiveGenerationConfig config) {

        LinkedList<File> sourceFiles = new LinkedList<File>();

        for (Iterator<URL> sources = config.getSource(); sources.hasNext();) {
            URL source = sources.next();
            sourceFiles.add(URLUtil.getFileFromURL(source));
        }

        LOGGER.info("Seeded with {} source paths:", sourceFiles.size());

        FileUtil.resolveRecursive((GenerationConfig)config, sourceFiles);

        LOGGER.info("Resolved {} schema files:", sourceFiles.size());

        for (Iterator<File> iterator = sourceFiles.iterator(); iterator.hasNext();) {
            File item = iterator.next();
            schemaStore.create(item.toURI());
        }

        LOGGER.info("Identified {} objects:", schemaStore.getSize());

        for (Iterator<Schema> schemaIterator = schemaStore.getSchemaIterator(); schemaIterator.hasNext(); ) {
            Schema schema = schemaIterator.next();
            currentDepth = 0;
            if( schema.getURI().getScheme().equals("file")) {
                String inputFile = schema.getURI().getPath();
                String resourcePath = dropSourcePathPrefix(inputFile, config.getSourceDirectory());
                for (String sourcePath : config.getSourcePaths()) {
                    resourcePath = dropSourcePathPrefix(resourcePath, sourcePath);
                }
                String outputFile = config.getTargetDirectory() + "/" + swapExtension(resourcePath, "json", "hql");

                LOGGER.info("Processing {}:", resourcePath);

                String resourceId = dropExtension(resourcePath).replace("/", "_");

                String resourceContent = generateResource(schema, resourceId);

                writeFile(outputFile, resourceContent);

                LOGGER.info("Wrote {}:", outputFile);
            }
        }
    }

    public String generateResource(Schema schema, String resourceId) {
        StringBuilder resourceBuilder = new StringBuilder();
        resourceBuilder.append("CREATE TABLE ");
        resourceBuilder.append(hqlEscape(resourceId));
        resourceBuilder.append(LS);
        resourceBuilder.append("(");
        resourceBuilder.append(LS);
        resourceBuilder = appendRootObject(resourceBuilder, schema, resourceId, ' ');
        resourceBuilder.append(")");
        resourceBuilder.append(LS);
        resourceBuilder.append("ROW FORMAT SERDE 'org.openx.data.jsonserde.JsonSerDe'");
        resourceBuilder.append(LS);
        resourceBuilder.append("WITH SERDEPROPERTIES (\"ignore.malformed.json\" = \"true\"");
        resourceBuilder.append(LS);
        resourceBuilder.append("STORED AS TEXTFILE");
        resourceBuilder.append(LS);
        resourceBuilder.append("LOCATION '${hiveconf:path}';");
        resourceBuilder.append(LS);
        return resourceBuilder.toString();
    }

    public StringBuilder appendRootObject(StringBuilder builder, Schema schema, String resourceId, Character seperator) {
        ObjectNode propertiesNode = schemaStore.resolveProperties(schema, null, resourceId);
        if( propertiesNode != null && propertiesNode.isObject() && propertiesNode.size() > 0) {
            builder = appendPropertiesNode(builder, schema, propertiesNode, seperator);
        }
        return builder;
    }

    private StringBuilder appendValueField(StringBuilder builder, Schema schema, String fieldId, FieldType fieldType, Character seperator) {
        // safe to append nothing
        checkNotNull(builder);
        builder.append(hqlEscape(fieldId));
        builder.append(seperator);
        builder.append(hqlType(fieldType));
        return builder;
    }

    public StringBuilder appendArrayItems(StringBuilder builder, Schema schema, String fieldId, ObjectNode itemsNode, Character seperator) {
        // not safe to append nothing
        checkNotNull(builder);
        if( itemsNode == null ) return builder;
        if( itemsNode.has("type")) {
            try {
                FieldType itemType = FieldUtil.determineFieldType(itemsNode);
                switch( itemType ) {
                    case OBJECT:
                        builder = appendArrayObject(builder, schema, fieldId, itemsNode, seperator);
                        break;
                    case ARRAY:
                        ObjectNode subArrayItems = (ObjectNode) itemsNode.get("items");
                        builder = appendArrayItems(builder, schema, fieldId, subArrayItems, seperator);
                        break;
                    default:
                        builder = appendArrayField(builder, schema, fieldId, itemType, seperator);
                }
            } catch (Exception e) {
                LOGGER.warn("No item type resolvable for {}", fieldId);
            }
        }
        checkNotNull(builder);
        return builder;
    }

    private StringBuilder appendArrayField(StringBuilder builder, Schema schema, String fieldId, FieldType fieldType, Character seperator) {
        // safe to append nothing
        checkNotNull(builder);
        checkNotNull(fieldId);
        builder.append(hqlEscape(fieldId));
        builder.append(seperator);
        builder.append("ARRAY<"+hqlType(fieldType)+">");
        checkNotNull(builder);
        return builder;
    }

    private StringBuilder appendArrayObject(StringBuilder builder, Schema schema, String fieldId, ObjectNode fieldNode, Character seperator) {
        // safe to append nothing
        checkNotNull(builder);
        checkNotNull(fieldNode);
        if( !Strings.isNullOrEmpty(fieldId)) {
            builder.append(hqlEscape(fieldId));
            builder.append(seperator);
        }
        builder.append("ARRAY");
        builder.append(LS);
        builder.append("<");
        builder.append(LS);
        ObjectNode propertiesNode = schemaStore.resolveProperties(schema, fieldNode, fieldId);
        builder = appendStructField(builder, schema, "", propertiesNode, ':');
        builder.append(">");
        checkNotNull(builder);
        return builder;
    }

    private StringBuilder appendStructField(StringBuilder builder, Schema schema, String fieldId, ObjectNode propertiesNode, Character seperator) {
        // safe to append nothing
        checkNotNull(builder);
        checkNotNull(propertiesNode);

        if( propertiesNode != null && propertiesNode.isObject() && propertiesNode.size() > 0 ) {

            currentDepth += 1;

            if( !Strings.isNullOrEmpty(fieldId)) {
                builder.append(hqlEscape(fieldId));
                builder.append(seperator);
            }
            builder.append("STRUCT");
            builder.append(LS);
            builder.append("<");
            builder.append(LS);

            builder = appendPropertiesNode(builder, schema, propertiesNode, ':');

            builder.append(">");
            builder.append(LS);

            currentDepth -= 1;

        }
        checkNotNull(builder);
        return builder;
    }

    private StringBuilder appendPropertiesNode(StringBuilder builder, Schema schema, ObjectNode propertiesNode, Character seperator) {
        checkNotNull(builder);
        checkNotNull(propertiesNode);
        Iterator<Map.Entry<String, JsonNode>> fields = propertiesNode.fields();
        Joiner joiner = Joiner.on(","+LS).skipNulls();
        List<String> fieldStrings = Lists.newArrayList();
        for( ; fields.hasNext(); ) {
            Map.Entry<String, JsonNode> field = fields.next();
            String fieldId = field.getKey();
            if( !config.getExclusions().contains(fieldId) && field.getValue().isObject()) {
                ObjectNode fieldNode = (ObjectNode) field.getValue();
                FieldType fieldType = FieldUtil.determineFieldType(fieldNode);
                if (fieldType != null ) {
                    switch (fieldType) {
                        case ARRAY:
                            ObjectNode itemsNode = (ObjectNode) fieldNode.get("items");
                            if( currentDepth <= config.getMaxDepth()) {
                                StringBuilder arrayItemsBuilder = appendArrayItems(new StringBuilder(), schema, fieldId, itemsNode, seperator);
                                if( !Strings.isNullOrEmpty(arrayItemsBuilder.toString())) {
                                    fieldStrings.add(arrayItemsBuilder.toString());
                                }
                            }
                            break;
                        case OBJECT:
                            ObjectNode childProperties = schemaStore.resolveProperties(schema, fieldNode, fieldId);
                            if( currentDepth < config.getMaxDepth()) {
                                StringBuilder structFieldBuilder = appendStructField(new StringBuilder(), schema, fieldId, childProperties, seperator);
                                if( !Strings.isNullOrEmpty(structFieldBuilder.toString())) {
                                    fieldStrings.add(structFieldBuilder.toString());
                                }
                            }
                            break;
                        default:
                            StringBuilder valueFieldBuilder = appendValueField(new StringBuilder(), schema, fieldId, fieldType, seperator);
                            if( !Strings.isNullOrEmpty(valueFieldBuilder.toString())) {
                                fieldStrings.add(valueFieldBuilder.toString());
                            }
                    }
                }
            }
        }
        builder.append(joiner.join(fieldStrings)).append(LS);
        Preconditions.checkNotNull(builder);
        return builder;
    }

    private static String hqlEscape( String fieldId ) {
        return "`"+fieldId+"`";
    }

    private static String hqlType( FieldType fieldType ) {
        switch( fieldType ) {
            case INTEGER:
                return "INT";
            case NUMBER:
                return "FLOAT";
            case OBJECT:
                return "STRUCT";
            default:
                return fieldType.name().toUpperCase();
        }
    }

}
