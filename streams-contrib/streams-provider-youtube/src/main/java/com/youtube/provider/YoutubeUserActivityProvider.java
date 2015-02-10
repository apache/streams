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

package com.youtube.provider;

import com.google.api.services.youtube.YouTube;
import org.apache.streams.core.StreamsDatum;
import org.apache.streams.google.gplus.configuration.UserInfo;
import org.apache.streams.util.api.requests.backoff.BackOffStrategy;
import org.apache.youtube.pojo.YoutubeConfiguration;

import java.util.concurrent.BlockingQueue;

public class YoutubeUserActivityProvider extends YoutubeProvider {

    public YoutubeUserActivityProvider(YoutubeConfiguration config) {
        super(config);
    }

    @Override
    protected Runnable getDataCollector(BackOffStrategy strategy, BlockingQueue<StreamsDatum> queue, YouTube youtube, UserInfo userInfo) {
        return new YoutubeUserActivityCollector(youtube, queue, strategy, userInfo, config);
    }
}
