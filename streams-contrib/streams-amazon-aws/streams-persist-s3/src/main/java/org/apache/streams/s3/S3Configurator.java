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
import com.google.common.base.Preconditions;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigRenderOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class S3Configurator {

    private final static Logger LOGGER = LoggerFactory.getLogger(S3Configurator.class);

    private final static ObjectMapper mapper = new ObjectMapper();

    public static S3Configuration detectConfiguration(Config s3) {

        S3Configuration s3Configuration = null;

        try {
            s3Configuration = mapper.readValue(s3.root().render(ConfigRenderOptions.concise()), S3Configuration.class);
        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.warn("Could not parse S3Configuration");
        }

        return s3Configuration;
    }

    public static S3ReaderConfiguration detectReaderConfiguration(Config s3) {

        S3ReaderConfiguration s3Configuration = null;

        try {
            s3Configuration = mapper.readValue(s3.root().render(ConfigRenderOptions.concise()), S3ReaderConfiguration.class);
        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.warn("Could not parse S3Configuration");
        }

        return s3Configuration;
    }

    public static S3WriterConfiguration detectWriterConfiguration(Config s3) {

        S3WriterConfiguration s3Configuration = null;

        try {
            s3Configuration = mapper.readValue(s3.root().render(ConfigRenderOptions.concise()), S3WriterConfiguration.class);
        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.warn("Could not parse S3Configuration");
        }

        Preconditions.checkArgument(s3Configuration.getWriterPath().endsWith("/"), s3Configuration.getWriterPath() + " must end with '/'");

        return s3Configuration;
    }

}
