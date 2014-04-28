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

import com.google.common.collect.Lists;
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
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Wrapper for the Sysomos API.
 */
public class SysomosProvider implements StreamsProvider {

    private final static Logger LOGGER = LoggerFactory.getLogger(SysomosProvider.class);

    private SysomosConfiguration config;

    private List<String> apiKeys;
    private List<ExecutorService> tasks = new LinkedList<ExecutorService>();
    private boolean started = false;

    public SysomosProvider(SysomosConfiguration sysomosConfiguration) {
        this.apiKeys = Lists.newArrayList();
    }

    public static final String BASE_URL_STRING = "http://api.sysomos.com/";
    private static final String DATE_FORMAT_STRING = "yyyy-MM-dd'T'hh:mm:ssZ";
    private static final String HEARTBEAT_INFO_URL = "http://api.sysomos.com/v1/heartbeat/info?apiKey={api_key}&hid={hid}";
    private static Pattern _pattern = Pattern.compile("code: ([0-9]+)");

    public static final int LATENCY = 10;

    private String apiKey;

    public SysomosConfiguration getConfig() {
        return config;
    }

    public void setConfig(SysomosConfiguration config) {
        this.config = config;
    }

    protected volatile Queue<StreamsDatum> providerQueue = new ConcurrentLinkedQueue<StreamsDatum>();

    SysomosProviderTask task;
    ScheduledExecutorService service;

    @Override
    public void startStream() {
        LOGGER.trace("Starting Producer");
        if(!started) {
            LOGGER.trace("Producer not started.  Initializing");
            service = Executors.newScheduledThreadPool(getConfig().getHeartbeatIds().size() + 1);
            for(String heartbeatId : getConfig().getHeartbeatIds()) {
                task = new SysomosProviderTask(this, heartbeatId);
                service.scheduleWithFixedDelay(task, 0, LATENCY, TimeUnit.SECONDS);
                LOGGER.info("Started producer for {} with service {}", getConfig().getApiKey(), service.toString());
                this.tasks.add(service);
            }
            started = true;
        }
    }

    @Override
    public StreamsResultSet readCurrent() {
        return null;
    }

    @Override
    public StreamsResultSet readNew(BigInteger bigInteger) {
        return null;
    }

    @Override
    public StreamsResultSet readRange(DateTime dateTime, DateTime dateTime2) {
        return null;
    }

    @Override
    public void prepare(Object configurationObject) {

    }

    @Override
    public void cleanUp() {

    }
}
