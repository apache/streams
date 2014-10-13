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

package org.apache.streams.hdfs;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigRenderOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by sblackmon on 12/10/13.
 */
public class HdfsConfigurator {

    private final static Logger LOGGER = LoggerFactory.getLogger(HdfsConfigurator.class);

    private final static ObjectMapper mapper = new ObjectMapper();

    public static HdfsConfiguration detectConfiguration(Config hdfs) {

        HdfsConfiguration hdfsConfiguration = null;

        try {
            hdfsConfiguration = mapper.readValue(hdfs.root().render(ConfigRenderOptions.concise()), HdfsConfiguration.class);
        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.warn("Could not parse HdfsConfiguration");
        }
        return hdfsConfiguration;
    }

    public static HdfsReaderConfiguration detectReaderConfiguration(Config hdfs) {

        return mapper.convertValue(detectConfiguration(hdfs), HdfsReaderConfiguration.class);
    }

    public static HdfsWriterConfiguration detectWriterConfiguration(Config hdfs) {

        return mapper.convertValue(detectConfiguration(hdfs), HdfsWriterConfiguration.class);
    }

}
