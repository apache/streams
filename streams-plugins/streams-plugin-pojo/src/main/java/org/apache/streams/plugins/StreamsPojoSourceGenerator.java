package org.apache.streams.plugins;

import com.google.common.base.Preconditions;
import org.jsonschema2pojo.Jsonschema2Pojo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

/**
 * Created by sblackmon on 4/20/16.
 */
public class StreamsPojoSourceGenerator implements Runnable {

    private final static Logger LOGGER = LoggerFactory.getLogger(StreamsPojoSourceGenerator.class);

    private final static String LS = System.getProperty("line.separator");

    private StreamsPojoGenerationConfig config;

    public static void main(String[] args) {
        StreamsPojoGenerationConfig config = new StreamsPojoGenerationConfig();

        String sourceDirectory = "./target/test-classes/activities";
        String targetDirectory = "./target/generated-sources/pojo";
        String targetPackage = "";

        if( args.length > 0 )
            sourceDirectory = args[0];
        if( args.length > 1 )
            targetDirectory = args[1];
        if( args.length > 2 )
            targetPackage = args[2];

        config.setSourceDirectory(sourceDirectory);
        config.setTargetPackage(targetPackage);
        config.setTargetDirectory(targetDirectory);

        StreamsPojoSourceGenerator streamsPojoSourceGenerator = new StreamsPojoSourceGenerator(config);
        streamsPojoSourceGenerator.run();

        return;
    }

    public StreamsPojoSourceGenerator(StreamsPojoGenerationConfig config) {
        this.config = config;
    }

    @Override
    public void run() {

        Preconditions.checkNotNull(config);

        try {
            Jsonschema2Pojo.generate(config);
        } catch (Throwable e) {
            LOGGER.error("{} {}", e.getClass(), e.getMessage());
        }
    }

    private void writeFile(String pojoFile, String pojoHive) {
        try {
            File path = new File(pojoFile);
            File dir = path.getParentFile();
            if( !dir.exists() )
                dir.mkdirs();
            Files.write(Paths.get(pojoFile), pojoHive.getBytes(), StandardOpenOption.CREATE_NEW);
        } catch (Exception e) {
            LOGGER.error("Write Exception: {}", e);
        }
    }
}
