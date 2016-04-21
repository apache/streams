package org.apache.streams.plugins;

import com.google.common.base.Preconditions;
import org.apache.maven.artifact.DependencyResolutionRequiredException;
import org.apache.maven.plugin.MojoExecutionException;
import org.jsonschema2pojo.Jsonschema2Pojo;
import org.jsonschema2pojo.maven.ProjectClasspath;
import org.jsonschema2pojo.util.URLUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import static org.apache.commons.lang.StringUtils.isNotBlank;

/**
 * Created by sblackmon on 4/20/16.
 */
public class StreamsPojoSourceGenerator implements Runnable {

    private final static Logger LOGGER = LoggerFactory.getLogger(StreamsPojoSourceGenerator.class);

    private final static String LS = System.getProperty("line.separator");

    private StreamsPojoSourceGeneratorMojo mojo;

    private StreamsPojoGenerationConfig config;

    public void main(String[] args) {
        StreamsPojoGenerationConfig config = new StreamsPojoGenerationConfig();

        String sourceDirectory = "./target/test-classes/activities";
        String targetDirectory = "./target/generated-sources/streams-plugin-pojo";
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
        Thread thread = new Thread(streamsPojoSourceGenerator);
        thread.start();
        try {
            thread.join();
        } catch (InterruptedException e) {
            LOGGER.error("InterruptedException", e);
        } catch (Exception e) {
            LOGGER.error("Exception", e);
        }
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
