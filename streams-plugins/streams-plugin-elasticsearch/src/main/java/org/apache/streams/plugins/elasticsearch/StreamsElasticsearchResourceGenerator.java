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
package org.apache.streams.plugins.elasticsearch;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.base.Joiner;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import org.apache.streams.jackson.StreamsJacksonMapper;
import org.apache.streams.util.schema.FieldType;
import org.apache.streams.util.schema.FieldUtil;
import org.apache.streams.util.schema.GenerationConfig;
import org.apache.streams.util.schema.Schema;
import org.apache.streams.util.schema.SchemaStore;
import org.apache.streams.util.schema.SchemaStoreImpl;
import org.jsonschema2pojo.util.URLUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.URI;
import java.net.URL;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.apache.streams.util.schema.FileUtil.dropExtension;
import static org.apache.streams.util.schema.FileUtil.dropSourcePathPrefix;
import static org.apache.streams.util.schema.FileUtil.resolveRecursive;
import static org.apache.streams.util.schema.FileUtil.swapExtension;
import static org.apache.streams.util.schema.FileUtil.writeFile;

/**
 * Created by sblackmon on 5/3/16.
 */
public class StreamsElasticsearchResourceGenerator implements Runnable {

    private final static Logger LOGGER = LoggerFactory.getLogger(StreamsElasticsearchResourceGenerator.class);

    ObjectMapper MAPPER = StreamsJacksonMapper.getInstance();

    private final static String LS = System.getProperty("line.separator");

    private StreamsElasticsearchGenerationConfig config;

    private SchemaStore schemaStore = new SchemaStoreImpl();

    private int currentDepth = 0;

    public static void main(String[] args) {
        StreamsElasticsearchGenerationConfig config = new StreamsElasticsearchGenerationConfig();

        String sourceDirectory = "src/main/jsonschema";
        String targetDirectory = "target/generated-resources/streams-plugin-elasticsearch";

        if( args.length > 0 )
            sourceDirectory = args[0];
        if( args.length > 1 )
            targetDirectory = args[1];

        config.setSourceDirectory(sourceDirectory);
        config.setTargetDirectory(targetDirectory);

        StreamsElasticsearchResourceGenerator streamsElasticsearchResourceGenerator = new StreamsElasticsearchResourceGenerator(config);
        streamsElasticsearchResourceGenerator.run();

    }

    public StreamsElasticsearchResourceGenerator(StreamsElasticsearchGenerationConfig config) {
        this.config = config;
    }

    public void run() {

        checkNotNull(config);

        generate(config);

    }

    public void generate(StreamsElasticsearchGenerationConfig config) {

        LinkedList<File> sourceFiles = new LinkedList<File>();

        for (Iterator<URL> sources = config.getSource(); sources.hasNext();) {
            URL source = sources.next();
            sourceFiles.add(URLUtil.getFileFromURL(source));
        }

        LOGGER.info("Seeded with {} source paths:", sourceFiles.size());

        resolveRecursive((GenerationConfig)config, sourceFiles);

        LOGGER.info("Resolved {} schema files:", sourceFiles.size());

        for (Iterator<File> iterator = sourceFiles.iterator(); iterator.hasNext();) {
            File item = iterator.next();
            schemaStore.create(item.toURI());
        }

        LOGGER.info("Identified {} objects:", schemaStore.getSize());

        StringBuilder typesContent = new StringBuilder();

        for (Iterator<Schema> schemaIterator = schemaStore.getSchemaIterator(); schemaIterator.hasNext(); ) {
            Schema schema = schemaIterator.next();
            currentDepth = 0;
            if( schema.getURI().getScheme().equals("file")) {
                String inputFile = schema.getURI().getPath();
                String resourcePath = dropSourcePathPrefix(inputFile, config.getSourceDirectory());
                for (String sourcePath : config.getSourcePaths()) {
                    resourcePath = dropSourcePathPrefix(resourcePath, sourcePath);
                }
                String outputFile = config.getTargetDirectory() + "/" + resourcePath;

                LOGGER.info("Processing {}:", resourcePath);

                String resourceId = schemaSymbol(schema);

                String resourceContent = generateResource(schema, resourceId);

                if( !Strings.isNullOrEmpty(resourceContent))
                    writeFile(outputFile, resourceContent);

                LOGGER.info("Wrote {}:", outputFile);
            }
        }

    }

    public String generateResource(Schema schema, String resourceId) {
        StringBuilder resourceBuilder = new StringBuilder();

        ObjectNode rootNode = (ObjectNode) schema.getContent();

        // remove java*
        // remove description
        // resolve all $ref
        // replace format: date with type: date
        // replace format: date-time with type: date
        // replace array of primitive with just primitive

        try {
            String objectString = MAPPER.writeValueAsString(rootNode);
            resourceBuilder.append(objectString);
        } catch (JsonProcessingException e) {
            LOGGER.error("{}: {}", e.getClass().getName(), e);
        }
        return resourceBuilder.toString();
    }

    public StringBuilder appendRootObject(StringBuilder builder, Schema schema, String resourceId, Character seperator) {
        ObjectNode propertiesNode = schemaStore.resolveProperties(schema, null, resourceId);
        if( propertiesNode.get("id") != null ) {
            builder.append("id text PRIMARY KEY,");
            builder.append(LS);
            propertiesNode.remove("id");
        }
        if( propertiesNode != null && propertiesNode.isObject() && propertiesNode.size() > 0) {
            builder = appendPropertiesNode(builder, schema, propertiesNode, seperator);
        }
        return builder;
    }

    private StringBuilder appendValueField(StringBuilder builder, Schema schema, String fieldId, FieldType fieldType, Character seperator) {
        // safe to append nothing
        checkNotNull(builder);
        builder.append(cqlEscape(fieldId));
        builder.append(seperator);
        builder.append(cqlType(fieldType));
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
                        Schema objectSchema = null;
                        URI parentURI = null;
                        if( itemsNode.has("$ref") || itemsNode.has("extends") ) {
                            JsonNode refNode = itemsNode.get("$ref");
                            JsonNode extendsNode = itemsNode.get("extends");
                            if (refNode != null && refNode.isValueNode())
                                parentURI = URI.create(refNode.asText());
                            else if (extendsNode != null && extendsNode.isObject())
                                parentURI = URI.create(extendsNode.get("$ref").asText());
                            URI absoluteURI;
                            if (parentURI.isAbsolute())
                                absoluteURI = parentURI;
                            else {
                                absoluteURI = schema.getURI().resolve(parentURI);
                                if (!absoluteURI.isAbsolute() || (absoluteURI.isAbsolute() && !schemaStore.getByUri(absoluteURI).isPresent() ))
                                    absoluteURI = schema.getParentURI().resolve(parentURI);
                            }
                            if (absoluteURI != null && absoluteURI.isAbsolute()) {
                                Optional<Schema> schemaLookup = schemaStore.getByUri(absoluteURI);
                                if (schemaLookup.isPresent()) {
                                    objectSchema = schemaLookup.get();
                                }
                            }
                        }
                        // have to resolve schema here

                        builder = appendArrayObject(builder, objectSchema, fieldId, seperator);
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
        builder.append(cqlEscape(fieldId));
        builder.append(seperator);
        builder.append("list<"+cqlType(fieldType)+">");
        checkNotNull(builder);
        return builder;
    }

    private StringBuilder appendArrayObject(StringBuilder builder, Schema schema, String fieldId, Character seperator) {
        // safe to append nothing
        checkNotNull(builder);
        String schemaSymbol = schemaSymbol(schema);
        if( !Strings.isNullOrEmpty(fieldId) && schemaSymbol != null ) {
            builder.append(cqlEscape(fieldId));
            builder.append(seperator);
            builder.append("list<" + schemaSymbol + ">");
            builder.append(LS);
        }
        checkNotNull(builder);
        return builder;
    }

    private StringBuilder appendSchemaField(StringBuilder builder, Schema schema, String fieldId, Character seperator) {
        // safe to append nothing
        checkNotNull(builder);
        String schemaSymbol = schemaSymbol(schema);
        if( !Strings.isNullOrEmpty(fieldId) && schemaSymbol != null ) {
            builder.append(cqlEscape(fieldId));
            builder.append(seperator);
            builder.append(schemaSymbol);
        }
        checkNotNull(builder);
        return builder;
    }

    /*
     can this be moved to streams-schemas if schemastore available in scope?
     maybe an interface?
     lot of boilerplate / reuse between plugins
     however treatment is way different when resolving a type symbol vs resolving and listing fields .
     */
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
                            Schema objectSchema = null;
                            URI parentURI = null;
                            if( fieldNode.has("$ref") || fieldNode.has("extends") ) {
                                JsonNode refNode = fieldNode.get("$ref");
                                JsonNode extendsNode = fieldNode.get("extends");
                                if (refNode != null && refNode.isValueNode())
                                    parentURI = URI.create(refNode.asText());
                                else if (extendsNode != null && extendsNode.isObject())
                                    parentURI = URI.create(extendsNode.get("$ref").asText());
                                URI absoluteURI;
                                if (parentURI.isAbsolute())
                                    absoluteURI = parentURI;
                                else {
                                    absoluteURI = schema.getURI().resolve(parentURI);
                                    if (!absoluteURI.isAbsolute() || (absoluteURI.isAbsolute() && !schemaStore.getByUri(absoluteURI).isPresent() ))
                                        absoluteURI = schema.getParentURI().resolve(parentURI);
                                }
                                if (absoluteURI != null && absoluteURI.isAbsolute()) {
                                    Optional<Schema> schemaLookup = schemaStore.getByUri(absoluteURI);
                                    if (schemaLookup.isPresent()) {
                                        objectSchema = schemaLookup.get();
                                    }
                                }
                            }
                            //ObjectNode childProperties = schemaStore.resolveProperties(schema, fieldNode, fieldId);
                            if( currentDepth < config.getMaxDepth()) {
                                StringBuilder structFieldBuilder = appendSchemaField(new StringBuilder(), objectSchema, fieldId, seperator);
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

    private static String cqlEscape( String fieldId ) {
        return "`"+fieldId+"`";
    }

    private static String cqlType( FieldType fieldType ) {
        switch( fieldType ) {
            case STRING:
                return "text";
            case INTEGER:
                return "int";
            case NUMBER:
                return "double";
            case OBJECT:
                return "tuple";
            case ARRAY:
                return "list";
            default:
                return fieldType.name().toUpperCase();
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
            return dropExtension(resourcePath).replace("/", "_");
        } else {
            return "IDK";
        }
    }
}
