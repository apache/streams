/*
Licensed to the Apache Software Foundation (ASF) under one
or more contributor license agreements. See the NOTICE file
distributed with this work for additional information
regarding copyright ownership. The ASF licenses this file
to you under the Apache License, Version 2.0 (the
"License"); you may not use this file except in compliance *
http://www.apache.org/licenses/LICENSE-2.0 *
Unless required by applicable law or agreed to in writing,
software distributed under the License is distributed on an
"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
KIND, either express or implied. See the License for the
specific language governing permissions and limitations
under the License. */
package org.apache.streams.instagram.provider.recentmedia;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Queues;
import com.google.common.collect.Sets;
import org.apache.streams.config.StreamsConfigurator;
import org.apache.streams.core.StreamsDatum;
import org.apache.streams.core.StreamsProvider;
import org.apache.streams.core.StreamsResultSet;
import org.apache.streams.instagram.*;
import org.apache.streams.instagram.provider.InstagramAbstractProvider;
import org.apache.streams.instagram.provider.InstagramDataCollector;
import org.apache.streams.instagram.provider.recentmedia.InstagramRecentMediaCollector;
import org.apache.streams.util.ComponentUtils;
import org.apache.streams.util.SerializationUtil;
import org.jinstagram.entity.users.feed.MediaFeedData;
import org.joda.time.DateTime;

import java.math.BigInteger;
import java.util.Collection;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Instagram {@link org.apache.streams.core.StreamsProvider} that provides the recent media data for a group of users
 */
public class InstagramRecentMediaProvider extends InstagramAbstractProvider {


    public InstagramRecentMediaProvider() {
    }

    public InstagramRecentMediaProvider(InstagramConfiguration config) {
        super(config);
    }

    @Override
    protected InstagramDataCollector getInstagramDataCollector() {
        return new InstagramRecentMediaCollector(super.dataQueue, super.config);
    }
}
