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
package org.apache.streams.s3;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.typesafe.config.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class S3Configurator {

    private final static Logger LOGGER = LoggerFactory.getLogger(S3Configurator.class);

    private final static ObjectMapper mapper = new ObjectMapper();

    public static S3Configuration detectConfiguration(Config s3) {

        S3Configuration s3Configuration = new S3Configuration();

        s3Configuration.setBucket(s3.getString("bucket"));
        s3Configuration.setKey(s3.getString("key"));
        s3Configuration.setSecretKey(s3.getString("secretKey"));

        // The Amazon S3 Library defaults to HTTPS
        String protocol = (!s3.hasPath("protocol") ? "https": s3.getString("protocol")).toLowerCase();

        if(!(protocol.equals("https") || protocol.equals("http"))) {
            // you must specify either HTTP or HTTPS
            throw new RuntimeException("You must specify either HTTP or HTTPS as a protocol");
        }

        s3Configuration.setProtocol(protocol.toLowerCase());

        return s3Configuration;
    }

    public static S3ReaderConfiguration detectReaderConfiguration(Config s3) {

        S3Configuration S3Configuration = detectConfiguration(s3);
        S3ReaderConfiguration s3ReaderConfiguration = mapper.convertValue(S3Configuration, S3ReaderConfiguration.class);

        s3ReaderConfiguration.setReaderPath(s3.getString("readerPath"));

        return s3ReaderConfiguration;
    }

    public static S3WriterConfiguration detectWriterConfiguration(Config s3) {

        S3Configuration s3Configuration = detectConfiguration(s3);
        S3WriterConfiguration s3WriterConfiguration  = mapper.convertValue(s3Configuration, S3WriterConfiguration.class);

        String rootPath = s3.getString("writerPath");

        // if the root path doesn't end in a '/' then we need to force the '/' at the end of the path.
        s3WriterConfiguration.setWriterPath(rootPath + (rootPath.endsWith("/") ? "" : "/"));

        s3WriterConfiguration.setWriterFilePrefix(s3.hasPath("writerFilePrefix") ? s3.getString("writerFilePrefix") : "default");

        if(s3.hasPath("maxFileSize"))
            s3WriterConfiguration.setMaxFileSize((long)s3.getInt("maxFileSize"));
        if(s3.hasPath("chunk"))
            s3WriterConfiguration.setChunk(s3.getBoolean("chunk"));

        return s3WriterConfiguration;
    }

}
