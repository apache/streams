/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
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
package org.apache.streams.util.schema;

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
 * FileUtil contains methods to assist in working with local schema files during
 * source or resource generation.
 */
public class FileUtil {

    private final static Logger LOGGER = LoggerFactory.getLogger(FileUtil.class);

    public static String dropSourcePathPrefix(String inputFile, String sourceDirectory) {
        if(Strings.isNullOrEmpty(sourceDirectory))
            return inputFile;
        else {
            try {
                if( inputFile.contains(sourceDirectory) && inputFile.indexOf(sourceDirectory) > 0) {
                    return inputFile.substring(inputFile.indexOf(sourceDirectory)+sourceDirectory.length()+1);
                }
            } catch( Throwable e ) {
                return inputFile;
            }
        }
        return inputFile;
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
