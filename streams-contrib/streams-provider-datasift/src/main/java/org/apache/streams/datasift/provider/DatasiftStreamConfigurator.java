/*
Licensed to the Apache Software Foundation (ASF) under one
or more contributor license agreements.  See the NOTICE file
distributed with this work for additional information
regarding copyright ownership.  The ASF licenses this file
to you under the Apache License, Version 2.0 (the
"License"); you may not use this file except in compliance
with the License.  You may obtain a copy of the License at

  http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing,
software distributed under the License is distributed on an
"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
KIND, either express or implied.  See the License for the
specific language governing permissions and limitations
under the License.
*/
package org.apache.streams.datasift.provider;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigRenderOptions;
import org.apache.streams.datasift.DatasiftConfiguration;
import org.apache.streams.datasift.util.StreamsDatasiftMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 */
public class DatasiftStreamConfigurator {

    private final static Logger LOGGER = LoggerFactory.getLogger(DatasiftStreamConfigurator.class);

    private static final ObjectMapper MAPPER = StreamsDatasiftMapper.getInstance();

    public static DatasiftConfiguration detectConfiguration(Config datasift) {

        DatasiftConfiguration datasiftConfiguration = null;

        try {
            datasiftConfiguration = MAPPER.readValue(datasift.root().render(ConfigRenderOptions.concise()), DatasiftConfiguration.class);
        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.warn("Could not parse datasiftConfiguration");
        }
        return datasiftConfiguration;
    }

}
