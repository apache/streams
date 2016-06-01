package org.apache.streams.plugins.hbase;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
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
public class StreamsHbaseResourceGenerator implements Runnable {

    private final static Logger LOGGER = LoggerFactory.getLogger(StreamsHbaseResourceGenerator.class);

    private final static String LS = System.getProperty("line.separator");

    private StreamsHbaseGenerationConfig config;

    private SchemaStore schemaStore = new SchemaStoreImpl();

    private int currentDepth = 0;

    public static void main(String[] args) {
        StreamsHbaseGenerationConfig config = new StreamsHbaseGenerationConfig();

        String sourceDirectory = "./target/test-classes/activities";
        String targetDirectory = "./target/generated-resources/hbase";

        if( args.length > 0 )
            sourceDirectory = args[0];
        if( args.length > 1 )
            targetDirectory = args[1];

        config.setSourceDirectory(sourceDirectory);
        config.setTargetDirectory(targetDirectory);

        StreamsHbaseResourceGenerator streamsHbaseResourceGenerator = new StreamsHbaseResourceGenerator(config);
        streamsHbaseResourceGenerator.run();

    }

    public StreamsHbaseResourceGenerator(StreamsHbaseGenerationConfig config) {
        this.config = config;
    }

    public void run() {

        checkNotNull(config);

        generate(config);

    }

    public void generate(StreamsHbaseGenerationConfig config) {

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

        for (Iterator<Schema> schemaIterator = schemaStore.getSchemaIterator(); schemaIterator.hasNext(); ) {
            Schema schema = schemaIterator.next();
            currentDepth = 0;
            if( schema.getURI().getScheme().equals("file")) {
                String inputFile = schema.getURI().getPath();
                String resourcePath = dropSourcePathPrefix(inputFile, config.getSourceDirectory());
                for (String sourcePath : config.getSourcePaths()) {
                    resourcePath = dropSourcePathPrefix(resourcePath, sourcePath);
                }
                String outputFile = config.getTargetDirectory() + "/" + swapExtension(resourcePath, "json", "txt");

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
        resourceBuilder.append("CREATE ");
        resourceBuilder = appendRootObject(resourceBuilder, schema, resourceId);
        return resourceBuilder.toString();
    }

    public StringBuilder appendRootObject(StringBuilder builder, Schema schema, String resourceId) {
        checkNotNull(builder);
        ObjectNode propertiesNode = schemaStore.resolveProperties(schema, null, resourceId);
        if( propertiesNode != null && propertiesNode.isObject() && propertiesNode.size() > 0) {

            List<String> fieldStrings = Lists.newArrayList();

            // table
            fieldStrings.add(hbaseEscape(schemaSymbol(schema)));

            // column family
            fieldStrings.add(hbaseEscape(schemaSymbol(schema)));

            // parent column family
            if( schema.getParent() != null )
                fieldStrings.add(hbaseEscape(schemaSymbol(schema.getParent())));

            // sub-object column families
            if( propertiesNode != null && propertiesNode.isObject() && propertiesNode.size() > 0 ) {

                Iterator<Map.Entry<String, JsonNode>> fields = propertiesNode.fields();
                Joiner joiner = Joiner.on(", ").skipNulls();
                for( ; fields.hasNext(); ) {
                    Map.Entry<String, JsonNode> field = fields.next();
                    String fieldId = field.getKey();
                    if( !config.getExclusions().contains(fieldId) && field.getValue().isObject()) {
                        ObjectNode fieldNode = (ObjectNode) field.getValue();
                        FieldType fieldType = FieldUtil.determineFieldType(fieldNode);
                        if (fieldType != null ) {
                            switch (fieldType) {
                                case OBJECT:
                                    fieldStrings.add(hbaseEscape(fieldId));
                            }
                        }
                    }
                }
                builder.append(joiner.join(fieldStrings));

            }
        }
        checkNotNull(builder);
        return builder;
    }

    private static String hbaseEscape( String fieldId ) {
        return "'"+fieldId+"'";
    }

    private String schemaSymbol( Schema schema ) {
        if (schema == null) return null;
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
