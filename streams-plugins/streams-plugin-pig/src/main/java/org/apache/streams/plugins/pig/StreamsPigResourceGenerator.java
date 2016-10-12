/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
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

package org.apache.streams.plugins.pig;

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
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.apache.streams.util.schema.FileUtil.dropExtension;
import static org.apache.streams.util.schema.FileUtil.dropSourcePathPrefix;
import static org.apache.streams.util.schema.FileUtil.swapExtension;
import static org.apache.streams.util.schema.FileUtil.writeFile;

public class StreamsPigResourceGenerator implements Runnable {

    private final static Logger LOGGER = LoggerFactory.getLogger(StreamsPigResourceGenerator.class);

    private final static String LS = System.getProperty("line.separator");

    private StreamsPigGenerationConfig config;

    private SchemaStore schemaStore = new SchemaStoreImpl();

    private int currentDepth = 0;

    public static void main(String[] args) {
        StreamsPigGenerationConfig config = new StreamsPigGenerationConfig();

        String sourceDirectory = "src/main/jsonschema";
        String targetDirectory = "target/generated-resources/pig-cli";

        if (args.length > 0)
            sourceDirectory = args[0];
        if (args.length > 1)
            targetDirectory = args[1];

        config.setSourceDirectory(sourceDirectory);
        config.setTargetDirectory(targetDirectory);

        StreamsPigResourceGenerator streamsPigResourceGenerator = new StreamsPigResourceGenerator(config);
        streamsPigResourceGenerator.run();

    }

    public StreamsPigResourceGenerator(StreamsPigGenerationConfig config) {
        this.config = config;
    }

    public void run() {

        checkNotNull(config);

        generate(config);

    }

    public void generate(StreamsPigGenerationConfig config) {

        LinkedList<File> sourceFiles = new LinkedList<File>();

        for (Iterator<URL> sources = config.getSource(); sources.hasNext(); ) {
            URL source = sources.next();
            sourceFiles.add(URLUtil.getFileFromURL(source));
        }

        LOGGER.info("Seeded with {} source paths:", sourceFiles.size());

        FileUtil.resolveRecursive((GenerationConfig) config, sourceFiles);

        LOGGER.info("Resolved {} schema files:", sourceFiles.size());

        for (Iterator<File> iterator = sourceFiles.iterator(); iterator.hasNext(); ) {
            File item = iterator.next();
            schemaStore.create(item.toURI());
        }

        LOGGER.info("Identified {} objects:", schemaStore.getSize());

        for (Iterator<Schema> schemaIterator = schemaStore.getSchemaIterator(); schemaIterator.hasNext(); ) {
            Schema schema = schemaIterator.next();
            currentDepth = 0;
            if (schema.getURI().getScheme().equals("file")) {
                String inputFile = schema.getURI().getPath();
                String resourcePath = dropSourcePathPrefix(inputFile, config.getSourceDirectory());
                for (String sourcePath : config.getSourcePaths()) {
                    resourcePath = dropSourcePathPrefix(resourcePath, sourcePath);
                }
                String outputFile = config.getTargetDirectory() + "/" + swapExtension(resourcePath, "json", "pig");

                LOGGER.info("Processing {}:", resourcePath);

                String resourceId = schemaSymbol(schema);

                String resourceContent = generateResource(schema, resourceId);

                writeFile(outputFile, resourceContent);

                LOGGER.info("Wrote {}:", outputFile);
            }
        }
    }

    public String generateResource(Schema schema, String resourceId) {
        StringBuilder resourceBuilder = new StringBuilder();
        resourceBuilder.append(pigEscape(resourceId));
        resourceBuilder.append(" = ");
        resourceBuilder.append("LOAD '' USING JsonLoader('");
        resourceBuilder = appendRootObject(resourceBuilder, schema, resourceId, ':');
        resourceBuilder.append("');");
        return resourceBuilder.toString();
    }

    public StringBuilder appendRootObject(StringBuilder builder, Schema schema, String resourceId, Character seperator) {
        ObjectNode propertiesNode = schemaStore.resolveProperties(schema, null, resourceId);
        if (propertiesNode != null && propertiesNode.isObject() && propertiesNode.size() > 0) {
            builder = appendPropertiesNode(builder, schema, propertiesNode, seperator);
        }
        return builder;
    }

    private StringBuilder appendPropertiesNode(StringBuilder builder, Schema schema, ObjectNode propertiesNode, Character seperator) {
        checkNotNull(builder);
        checkNotNull(propertiesNode);
        Iterator<Map.Entry<String, JsonNode>> fields = propertiesNode.fields();
        Joiner joiner = Joiner.on(", ").skipNulls();
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
                            ObjectNode resolvedItems = schemaStore.resolveItems(schema, fieldNode, fieldId);
                            if( resolvedItems != null && currentDepth <= config.getMaxDepth()) {
                                StringBuilder arrayItemsBuilder = appendArrayItems(new StringBuilder(), schema, fieldId, resolvedItems, seperator);
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
        joiner.appendTo(builder, fieldStrings);
        Preconditions.checkNotNull(builder);
        return builder;
    }

    private StringBuilder appendValueField(StringBuilder builder, Schema schema, String fieldId, FieldType fieldType, Character seperator) {
        // safe to append nothing
        checkNotNull(builder);
        builder.append(pigEscape(fieldId));
        builder.append(seperator);
        builder.append(pigType(fieldType));
        return builder;
    }

    public StringBuilder appendArrayItems(StringBuilder builder, Schema schema, String fieldId, ObjectNode itemsNode, Character seperator) {
        // not safe to append nothing
        checkNotNull(builder);
        if( itemsNode == null ) return builder;
        FieldType itemType = FieldUtil.determineFieldType(itemsNode);
        try {
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

        checkNotNull(builder);
        return builder;
    }

    private StringBuilder appendArrayField(StringBuilder builder, Schema schema, String fieldId, FieldType fieldType, Character seperator) {
        // safe to append nothing
        checkNotNull(builder);
        checkNotNull(fieldId);
        builder.append("{t: (");
        builder.append(pigEscape(fieldId));
        builder.append(seperator);
        builder.append(pigType(fieldType));
        builder.append(")}");
        checkNotNull(builder);
        return builder;
    }

    private StringBuilder appendArrayObject(StringBuilder builder, Schema schema, String fieldId, ObjectNode fieldNode, Character seperator) {
        // safe to append nothing
        checkNotNull(builder);
        checkNotNull(fieldId);
        checkNotNull(fieldNode);
        ObjectNode propertiesNode = schemaStore.resolveProperties(schema, fieldNode, fieldId);
        if( propertiesNode.size() > 0 ) {
            builder.append("{t: (");
            builder = appendStructField(builder, schema, "", propertiesNode, ':');
            builder.append(")}");
        }
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
                builder.append(pigEscape(fieldId));
                builder.append(seperator);
                builder.append("(");
                builder = appendPropertiesNode(builder, schema, propertiesNode, ':');
                builder.append(")");
            }

            currentDepth -= 1;

        }
        checkNotNull(builder);
        return builder;
    }

    private static String pigEscape( String fieldId ) {
        return fieldId;
    }

    private static String pigType( FieldType fieldType ) {
        switch( fieldType ) {
            case STRING:
                return "chararray";
            case INTEGER:
                return "int";
            case NUMBER:
                return "double";
            case OBJECT:
                return "tuple";
            default:
                return fieldType.name().toLowerCase();
        }
    }

    private String schemaSymbol( Schema schema ) {
        if (schema == null) return null;
        // this needs to return whatever
        if (schema.getURI().getScheme().equals("file")) {
            String inputFile = schema.getURI().getPath();
            String resourcePath = dropSourcePathPrefix(inputFile, config.getSourceDirectory());
            for (String sourcePath : config.getSourcePaths()) {
                resourcePath = dropSourcePathPrefix(resourcePath, sourcePath);
            }
            return dropExtension(resourcePath).replace("/", "_").replace("-", "");
        } else {
            return "IDK";
        }
    }
}
