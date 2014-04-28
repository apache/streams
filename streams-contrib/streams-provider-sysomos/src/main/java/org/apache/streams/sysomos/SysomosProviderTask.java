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

import com.sysomos.SysomosConfiguration;
import org.apache.commons.io.IOUtils;
import org.apache.streams.core.StreamsDatum;
import org.apache.streams.core.StreamsProvider;
import org.apache.streams.core.StreamsResultSet;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Queue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Wrapper for the Sysomos API.
 */
public class SysomosProviderTask implements Runnable {

    private final static Logger LOGGER = LoggerFactory.getLogger(SysomosProviderTask.class);

    private SysomosConfiguration config;

    private SysomosProvider provider;

    private SysomosClient client;

    private String heartbeatId;

    public SysomosProviderTask(SysomosProvider provider, String heartbeatId) {
        this.provider = provider;
        this.heartbeatId = heartbeatId;
    }

    @Override
    public void run() {

        client = new SysomosClient(provider.getConfig().getApiKey());

    }
}
