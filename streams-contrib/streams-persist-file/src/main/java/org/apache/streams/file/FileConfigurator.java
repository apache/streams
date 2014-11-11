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

package org.apache.streams.file;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigRenderOptions;
import org.apache.streams.config.StreamsConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by sblackmon on 12/10/13.
 */
public class FileConfigurator {

    private final static Logger LOGGER = LoggerFactory.getLogger(FileConfigurator.class);

    private final static ObjectMapper mapper = new ObjectMapper();

    public static FileConfiguration detectConfiguration(Config kafka) {

        FileConfiguration fileConfiguration = null;

        try {
            fileConfiguration = mapper.readValue(kafka.root().render(ConfigRenderOptions.concise()), FileConfiguration.class);
        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.warn("Could not parse FileConfiguration");
        }

        return fileConfiguration;
    }

}
