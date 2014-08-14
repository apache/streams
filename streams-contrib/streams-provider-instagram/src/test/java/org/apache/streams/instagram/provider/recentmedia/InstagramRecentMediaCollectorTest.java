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

import com.carrotsearch.randomizedtesting.RandomizedTest;
import com.carrotsearch.randomizedtesting.annotations.Repeat;
import com.google.common.collect.Lists;
import com.google.common.collect.Queues;
import com.google.common.collect.Sets;
import org.apache.streams.core.StreamsDatum;
import org.apache.streams.instagram.InstagramConfiguration;
import org.apache.streams.instagram.User;
import org.apache.streams.instagram.UsersInfo;
import org.jinstagram.Instagram;
import org.jinstagram.entity.common.Pagination;
import org.jinstagram.entity.users.feed.MediaFeed;
import org.jinstagram.entity.users.feed.MediaFeedData;
import org.jinstagram.exceptions.InstagramBadRequestException;
import org.jinstagram.exceptions.InstagramException;
import org.jinstagram.exceptions.InstagramRateLimitException;
import org.joda.time.DateTime;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

import static org.mockito.Matchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests for {@link org.apache.streams.instagram.provider.recentmedia.InstagramRecentMediaCollector}
 */
public class InstagramRecentMediaCollectorTest extends RandomizedTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(InstagramRecentMediaCollectorTest.class);

    private int expectedDataCount = 0;




    @Test
    @Repeat(iterations = 3)
    public void testRun() {
        this.expectedDataCount = 0;
        Queue<StreamsDatum> data = Queues.newConcurrentLinkedQueue();
        InstagramConfiguration config = new InstagramConfiguration();
        UsersInfo usersInfo = new UsersInfo();
        config.setUsersInfo(usersInfo);
        Set<User> users = creatUsers(randomIntBetween(0, 100));
        usersInfo.setUsers(users);

        final Instagram mockInstagram = createMockInstagramClient();
        InstagramRecentMediaCollector collector = new InstagramRecentMediaCollector(data, config) {
            @Override
            protected Instagram getNextInstagramClient() {
                return mockInstagram;
            }
        };
        assertFalse(collector.isCompleted());
        collector.run();
        assertTrue(collector.isCompleted());
        assertEquals(this.expectedDataCount, data.size());
    }

    private Instagram createMockInstagramClient() {
        final Instagram instagramClient = mock(Instagram.class);
        try {
            final InstagramException mockException = mock(InstagramException.class);
            when(mockException.getRemainingLimitStatus()).thenReturn(-1);
            when(mockException.getMessage()).thenReturn("MockInstagramException message");
            when(instagramClient.getRecentMediaFeed(anyLong(), anyInt(), anyString(), anyString(), any(Date.class), any(Date.class))).thenAnswer(new Answer<MediaFeed>() {
                @Override
                public MediaFeed answer(InvocationOnMock invocationOnMock) throws Throwable {
                    if (randomInt(20) == 0) { //5% throw exceptions
                        int type = randomInt(4);
                        if (type == 0)
                            throw mock(InstagramRateLimitException.class);
                        else if (type == 1)
                            throw mock(InstagramBadRequestException.class);
                        else if (type == 2)
                            throw mock(InstagramException.class);
                        else
                            throw new Exception();
                    } else {
                        return createRandomMockMediaFeed();
                    }
                }
            });
            when(instagramClient.getRecentMediaNextPage(any(Pagination.class))).thenAnswer(new Answer<MediaFeed>() {
                @Override
                public MediaFeed answer(InvocationOnMock invocationOnMock) throws Throwable {
                    return createRandomMockMediaFeed();
                }
            });
        } catch (InstagramException ie) {
            fail("Failed to create mock instagram client.");
        }
        return instagramClient;
    }

    private Set<User> creatUsers(int numUsers) {
        Set<User> users = Sets.newHashSet();
        for(int i=0; i < numUsers; ++i) {
            User user = new User();
            user.setUserId(Integer.toString(randomInt()));
            if(randomInt(2) == 0) {
                user.setAfterDate(DateTime.now().minusSeconds(randomIntBetween(0, 1000)));
            }
            if(randomInt(2) == 0) {
                user.setBeforeDate(DateTime.now());
            }
            users.add(user);
        }
        return users;
    }

    private MediaFeed createRandomMockMediaFeed() throws InstagramException {
        MediaFeed feed = mock(MediaFeed.class);
        when(feed.getData()).thenReturn(createData(randomInt(100)));
        Pagination pagination = mock(Pagination.class);
        if(randomInt(2) == 0) {
            when(pagination.hasNextPage()).thenReturn(true);
        } else {
            when(pagination.hasNextPage()).thenReturn(false);
        }
        when(feed.getPagination()).thenReturn(pagination);
        return feed;
    }

    private List<MediaFeedData> createData(int size) {
        List<MediaFeedData> data = Lists.newLinkedList();
        for(int i=0; i < size; ++i) {
            data.add(mock(MediaFeedData.class));
        }
        this.expectedDataCount += size;
        return data;
    }

}
