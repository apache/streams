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

package org.apache.streams.sysomos;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides a {@link java.lang.Runnable} query mechanism for grabbing documents from the Sysomos API
 */
public class SysomosHeartbeatTask implements Runnable {

    private final static Logger LOGGER = LoggerFactory.getLogger(SysomosHeartbeatTask.class);

    private SysomosProvider provider;
    private SysomosClient client;
    private String heartbeatId;
    private long maxApiBatch;
    private long currentOffset = 0L;

    public SysomosHeartbeatTask(SysomosProvider provider, SysomosClient client, String heartbeatId, long maxApiBatch) {
        this.provider = provider;
        this.client = client;
        this.heartbeatId = heartbeatId;
        this.maxApiBatch = maxApiBatch;
    }

    @Override
    public void run() {

        client = new SysomosClient(provider.getConfig().getApiKey());

    }
}
