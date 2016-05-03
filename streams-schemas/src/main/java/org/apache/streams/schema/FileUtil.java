package org.apache.streams.schema;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.List;

/**
 * Created by steve on 5/1/16.
 */
public class FileUtil {

    private final static Logger LOGGER = LoggerFactory.getLogger(FileUtil.class);

    public static String dropSourcePathPrefix(String inputFile, String sourceDirectory) {
        if(Strings.isNullOrEmpty(sourceDirectory))
            return inputFile;
        else if( inputFile.contains(sourceDirectory) ) {
            return inputFile.substring(inputFile.indexOf(sourceDirectory)+sourceDirectory.length()+1);
        } else return inputFile;
    }

    public static String swapExtension(String inputFile, String originalExtension, String newExtension) {
        if(inputFile.endsWith("."+originalExtension))
            return inputFile.replace("."+originalExtension, "."+newExtension);
        else return inputFile;
    }

    public static String dropExtension(String inputFile) {
        if(inputFile.contains("."))
            return inputFile.substring(0, inputFile.lastIndexOf("."));
        else return inputFile;
    }

    public static void writeFile(String resourceFile, String resourceContent) {
        try {
            File path = new File(resourceFile);
            File dir = path.getParentFile();
            if( !dir.exists() )
                dir.mkdirs();
            Files.write(Paths.get(resourceFile), resourceContent.getBytes(), StandardOpenOption.CREATE_NEW);
        } catch (Exception e) {
            LOGGER.error("Write Exception: {}", e);
        }
    }

    public static void resolveRecursive(GenerationConfig config, List<File> schemaFiles) {

        Preconditions.checkArgument(schemaFiles.size() > 0);
        int i = 0;
        while( schemaFiles.size() > i) {
            File child = schemaFiles.get(i);
            if (child.isDirectory()) {
                schemaFiles.addAll(Arrays.asList(child.listFiles(config.getFileFilter())));
                schemaFiles.remove(child);
            } else {
                i += 1;
            }
        }

    }
}
