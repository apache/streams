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

package org.apache.streams.sysomos.provider;

import org.apache.streams.sysomos.data.HeartbeatInfo;
import org.apache.streams.sysomos.util.SysomosUtils;

import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Wrapper for the Sysomos API.
 */
public class SysomosClient {

    public static final String BASE_URL_STRING = "http://api.sysomos.com/";
    private static final String HEARTBEAT_INFO_URL = "http://api.sysomos.com/v1/heartbeat/info?apiKey={api_key}&hid={hid}";

    private String apiKey;

    private HttpURLConnection client;

    public SysomosClient(String apiKey) {
        this.apiKey = apiKey;
    }

    public HeartbeatInfo getHeartbeatInfo(String hid) throws Exception {
        String urlString = HEARTBEAT_INFO_URL.replace("{api_key}", this.apiKey);
        urlString = urlString.replace("{hid}", hid);
        String xmlResponse = SysomosUtils.queryUrl(new URL(urlString));
        return new HeartbeatInfo(xmlResponse);
    }

    public RequestBuilder createRequestBuilder() {
        return new ContentRequestBuilder(BASE_URL_STRING, this.apiKey);
    }

}
