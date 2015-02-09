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

import com.google.api.client.util.Maps;
import com.google.api.client.util.Sets;
import com.google.api.services.youtube.YouTube;
import com.google.common.collect.Lists;
import org.apache.streams.core.StreamsDatum;
import org.apache.streams.google.gplus.configuration.UserInfo;
import org.apache.streams.util.api.requests.backoff.BackOffStrategy;
import org.apache.youtube.pojo.YoutubeConfiguration;
import org.joda.time.DateTime;
import org.junit.Test;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.BlockingQueue;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;

public class YoutubeProviderTest {

    /**
     * Test that every collector will be run and that data queued from the collectors will be processed.
     */
    @Test
    public void testDataCollectorRunsPerUser() {
        Random r = new Random(System.currentTimeMillis());
        int numUsers = r.nextInt(1000);
        List<UserInfo> userList = Lists.newLinkedList();

        for(int i=0; i < numUsers; ++i) {
            userList.add(new UserInfo());
        }

        YoutubeConfiguration config = new YoutubeConfiguration();
        config.setYoutubeUsers(userList);
        config.setApiKey("API_KEY");
        YoutubeProvider provider = buildProvider(config);

        try {
            provider.prepare(null);
            provider.startStream();
            int datumCount = 0;
            while(provider.isRunning()) {
                datumCount += provider.readCurrent().size();
            }
            assertEquals(numUsers, datumCount);
        } finally {
            provider.cleanUp();
        }
    }

    @Test
    public void testConfigSetterGetter() {
        YoutubeConfiguration config = new YoutubeConfiguration();
        config.setApiKey("API_KEY");
        config.setVersion("fake_version_1");
        YoutubeConfiguration newConfig = new YoutubeConfiguration();
        newConfig.setApiKey("API_KEY");
        config.setVersion("fake_version_2");

        YoutubeProvider provider = buildProvider(config);

        assertEquals(provider.getConfig(), config);

        provider.setConfig(newConfig);
        assertEquals(provider.getConfig(), newConfig);
    }

    @Test
    public void testUserInfoWithDefaultDates() {
        YoutubeConfiguration config = new YoutubeConfiguration();
        config.setApiKey("API_KEY");
        YoutubeProvider provider = buildProvider(config);

        DateTime afterDate = new DateTime(System.currentTimeMillis());
        DateTime beforeDate = afterDate.minus(10000);

        provider.setDefaultAfterDate(afterDate);
        provider.setDefaultBeforeDate(beforeDate);

        Set<String> users = Sets.newHashSet();
        users.add("test_user_1");
        users.add("test_user_2");
        users.add("test_user_3");

        provider.setUserInfoWithDefaultDates(users);

        List<UserInfo> youtubeUsers = provider.getConfig().getYoutubeUsers();

        for(UserInfo user : youtubeUsers) {
            assert(user.getAfterDate().equals(afterDate));
            assert(user.getBeforeDate().equals(beforeDate));
        }
    }

    @Test
    public void testUserInfoWithAfterDate() {
        YoutubeConfiguration config = new YoutubeConfiguration();
        config.setApiKey("API_KEY");
        YoutubeProvider provider = buildProvider(config);

        Map<String, DateTime> users = Maps.newHashMap();
        users.put("user1", new DateTime(System.currentTimeMillis()));
        users.put("user3", new DateTime(System.currentTimeMillis()));
        users.put("user4", new DateTime(System.currentTimeMillis()));

        provider.setUserInfoWithAfterDate(users);

        List<UserInfo> youtubeUsers = provider.getConfig().getYoutubeUsers();

        for(UserInfo user : youtubeUsers) {
            assert(user.getAfterDate().equals(users.get(user.getUserId())));
        }
    }

    private YoutubeProvider buildProvider(YoutubeConfiguration config) {
        return new YoutubeProvider(config) {

            @Override
            protected YouTube createYouTubeClient() throws IOException {
                return mock(YouTube.class);
            }

            @Override
            protected Runnable getDataCollector(BackOffStrategy strategy, BlockingQueue<StreamsDatum> queue, YouTube youtube, UserInfo userInfo) {
                final BlockingQueue<StreamsDatum> q = queue;
                return new Runnable() {
                    @Override
                    public void run() {
                        try {
                            q.put(new StreamsDatum(null));
                        } catch (InterruptedException ie) {
                            fail("Test was interrupted");
                        }
                    }
                };
            }
        };
    }
}