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

package org.apache.streams.data.moreover;

import com.google.common.collect.Lists;
import com.typesafe.config.Config;
import org.apache.streams.moreover.MoreoverConfiguration;
import org.apache.streams.moreover.MoreoverKeyData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Created by sblackmon on 12/10/13.
 */
public class MoreoverConfigurator {

    private final static Logger LOGGER = LoggerFactory.getLogger(MoreoverConfigurator.class);

    public static MoreoverConfiguration detectConfiguration(Config moreover) {

        MoreoverConfiguration moreoverConfiguration = new MoreoverConfiguration();

        List<MoreoverKeyData> apiKeys = Lists.newArrayList();

        Config apiKeysConfig = moreover.getConfig("apiKeys");

        if( !apiKeysConfig.isEmpty())
            for( String apiKeyId : apiKeysConfig.root().keySet() ) {
                Config apiKeyConfig = apiKeysConfig.getConfig(apiKeyId);
                apiKeys.add(new MoreoverKeyData()
                        .withId(apiKeyConfig.getString("key"))
                        .withKey(apiKeyConfig.getString("key"))
                        );
            }
        moreoverConfiguration.setApiKeys(apiKeys);

        return moreoverConfiguration;
    }

}
