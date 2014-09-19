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

import com.google.common.collect.Lists;
import org.apache.streams.core.StreamsDatum;
import org.apache.streams.instagram.InstagramConfiguration;
import org.apache.streams.instagram.User;
import org.apache.streams.instagram.provider.InstagramDataCollector;
import org.jinstagram.entity.users.basicinfo.UserInfo;
import org.jinstagram.entity.users.basicinfo.UserInfoData;
import org.jinstagram.exceptions.InstagramBadRequestException;
import org.jinstagram.exceptions.InstagramRateLimitException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Queue;

/**
 * InstagramDataCollector that pulls UserInfoData from Instagram
 * @see org.apache.streams.instagram.provider.InstagramDataCollector
 */
public class InstagramUserInfoCollector extends InstagramDataCollector<UserInfoData>{

    private static final Logger LOGGER = LoggerFactory.getLogger(InstagramUserInfoCollector.class);
    protected static final int MAX_ATTEMPTS = 5;

    private int consecutiveErrorCount;

    public InstagramUserInfoCollector(Queue<StreamsDatum> dataQueue, InstagramConfiguration config) {
        super(dataQueue, config);
        this.consecutiveErrorCount = 0;
    }

    @Override
    protected void collectInstagramDataForUser(User user) throws Exception {
        int attempt = 0;
        boolean successful = false;
        UserInfo userInfo = null;
        while(!successful && attempt < MAX_ATTEMPTS) {
            ++attempt;
            try {
                userInfo = getNextInstagramClient().getUserInfo(Long.valueOf(user.getUserId()));
            } catch (Exception e) {
                if(e instanceof InstagramRateLimitException) {
                    LOGGER.warn("Hit rate limit exception, backing off.");
                    super.backOffStrategy.backOff();
                } else if(e instanceof InstagramBadRequestException) {
                    LOGGER.error("Sent a bad request to Instagram, skipping user : {}", user.getUserId());
                    attempt = MAX_ATTEMPTS;
                    ++this.consecutiveErrorCount;
                } else {
                    LOGGER.error("Expection while polling instagram : {}", e);
                    ++this.consecutiveErrorCount;
                }
                if(this.consecutiveErrorCount >= Math.max(super.numAvailableTokens(), MAX_ATTEMPTS * 2)) {
                    LOGGER.error("Consecutive Errors above acceptable limits, ending collection of data.");
                    throw new Exception("Consecutive Errors above acceptable limits : "+this.consecutiveErrorCount);
                }
            }
            if(successful = (userInfo != null)) {
                this.consecutiveErrorCount = 0;
                List<UserInfoData> data = Lists.newLinkedList();
                data.add(userInfo.getData());
                super.queueData(data, user.getUserId());
            }
        }
        if(attempt == MAX_ATTEMPTS) {
            LOGGER.error("Failed to collect data for user : {}", user.getUserId());
        }
    }

    @Override
    protected StreamsDatum convertToStreamsDatum(UserInfoData item) {
        return new StreamsDatum(item, Long.toString(item.getId()));
    }


}
