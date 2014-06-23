/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
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

package org.apache.streams.sysomos.config;

import com.sysomos.SysomosConfiguration;
import com.typesafe.config.Config;

/**
 * Creates a {@link com.sysomos.SysomosConfiguration} instance from a {@link com.typesafe.config.Config} object.
 */
public class SysomosConfigurator {
    public static SysomosConfiguration detectConfiguration(Config config) {
        SysomosConfiguration sysomos = new SysomosConfiguration();
        sysomos.setHeartbeatIds(config.getStringList("heartbeatIds"));
        sysomos.setApiBatchSize(config.getLong("apiBatchSize"));
        sysomos.setApiKey(config.getString("apiKey"));
        sysomos.setMinDelayMs(config.getLong("minDelayMs"));
        sysomos.setScheduledDelayMs(config.getLong("scheduledDelayMs"));
        sysomos.setMaxBatchSize(config.getLong("maxBatchSize"));
        return sysomos;
    }
}
