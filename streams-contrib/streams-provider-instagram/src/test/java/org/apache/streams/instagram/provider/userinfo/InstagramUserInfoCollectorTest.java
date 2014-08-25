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
package org.apache.streams.instagram.provider.userinfo;

import com.carrotsearch.randomizedtesting.RandomizedTest;
import com.carrotsearch.randomizedtesting.annotations.Repeat;
import com.google.common.collect.Queues;
import org.apache.streams.core.StreamsDatum;
import org.apache.streams.instagram.InstagramConfiguration;
import org.apache.streams.instagram.User;
import org.apache.streams.instagram.UsersInfo;
import org.jinstagram.Instagram;
import org.jinstagram.entity.users.basicinfo.UserInfo;
import org.jinstagram.entity.users.basicinfo.UserInfoData;
import org.jinstagram.exceptions.InstagramBadRequestException;
import org.jinstagram.exceptions.InstagramException;
import org.jinstagram.exceptions.InstagramRateLimitException;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.util.HashSet;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link org.apache.streams.instagram.provider.userinfo.InstagramUserInfoCollector}
 */
public class InstagramUserInfoCollectorTest extends RandomizedTest {

    private boolean succesful;

    @Test
    public void testConvertToStreamsDatum() {
        InstagramUserInfoCollector collector = new InstagramUserInfoCollector(new ConcurrentLinkedQueue<StreamsDatum>(), createNonNullConfiguration());
        UserInfoData userInfoData = mock(UserInfoData.class);
        long id = 1;
        when(userInfoData.getId()).thenReturn(id);
        StreamsDatum datum = collector.convertToStreamsDatum(userInfoData);
        assertNotNull(datum);
        assertEquals(userInfoData, datum.document);
        assertEquals(Long.toString(userInfoData.getId()), datum.getId());
    }

    @Test
    @Repeat(iterations = 3)
    public void testcollectInstagramDataForUser() {
        succesful = false;
        Queue<StreamsDatum> datums = Queues.newConcurrentLinkedQueue();
        final Instagram client = createMockClient();
        InstagramUserInfoCollector collector = new InstagramUserInfoCollector(datums, createNonNullConfiguration()) {
            @Override
            protected Instagram getNextInstagramClient() {
                return client;
            }
        };
        if(succesful) {
            assertEquals(1, datums.size());
        } else {
            assertEquals(0, datums.size());
        }
    }

    private Instagram createMockClient() {
        Instagram client = mock(Instagram.class);
        try {
            when(client.getUserInfo(anyLong())).thenAnswer(new Answer<UserInfo>() {
                @Override
                public UserInfo answer(InvocationOnMock invocationOnMock) throws Throwable {
                    int exception = randomInt(10);
                    if (exception == 0) {
                        throw mock(InstagramRateLimitException.class);
                    } else if (exception == 1) {
                        throw mock(InstagramBadRequestException.class);
                    } else if (exception == 2) {
                        throw mock(InstagramException.class);
                    } else {
                        UserInfo info = mock(UserInfo.class);
                        UserInfoData data = mock(UserInfoData.class);
                        when(data.getId()).thenReturn(randomLong());
                        when(info.getData()).thenReturn(data);
                        succesful = true;
                        return info;
                    }
                }
            });
        } catch (InstagramException e) {
            fail("Never should throw exception creating mock instagram object");
        }
        return client;
    }

    private InstagramConfiguration createNonNullConfiguration() {
        InstagramConfiguration configuration = new InstagramConfiguration();
        UsersInfo info = new UsersInfo();
        configuration.setUsersInfo(info);
        info.setUsers(new HashSet<User>());
        info.setAuthorizedTokens(new HashSet<String>());
        return configuration;
    }

}
