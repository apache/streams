package org.apache.streams.datasift.provider;

/*
 * #%L
 * streams-provider-datasift
 * %%
 * Copyright (C) 2013 - 2014 Apache Streams Project
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import com.typesafe.config.Config;
import org.apache.streams.datasift.DatasiftConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by sblackmon on 12/10/13.
 */
public class DatasiftStreamConfigurator {

    private final static Logger LOGGER = LoggerFactory.getLogger(DatasiftStreamConfigurator.class);

    public static DatasiftConfiguration detectConfiguration(Config datasift) {

        DatasiftConfiguration datasiftConfiguration = new DatasiftConfiguration();

        datasiftConfiguration.setApiKey(datasift.getString("apiKey"));
        datasiftConfiguration.setUserName(datasift.getString("userName"));
        datasiftConfiguration.setStreamHash(datasift.getStringList("hashes"));

        return datasiftConfiguration;
    }

}
