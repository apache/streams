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

import org.apache.streams.core.StreamsDatum;
import org.apache.streams.instagram.api.Instagram;
import org.apache.streams.instagram.api.UserInfoResponse;
import org.apache.streams.instagram.config.InstagramConfiguration;
import org.apache.streams.instagram.config.InstagramUserInfoProviderConfiguration;
import org.apache.streams.instagram.pojo.UserInfo;
import org.apache.streams.instagram.provider.InstagramDataCollector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

/**
 * InstagramDataCollector that pulls UserInfoData from Instagram
 * @see org.apache.streams.instagram.provider.InstagramDataCollector
 */
public class InstagramUserInfoCollector extends InstagramDataCollector<UserInfo> {

  private static final Logger LOGGER = LoggerFactory.getLogger(InstagramUserInfoCollector.class);
  protected static final int MAX_ATTEMPTS = 5;

  private int consecutiveErrorCount;

  InstagramUserInfoProviderConfiguration config;

  public InstagramUserInfoCollector(Instagram instagram, Queue<StreamsDatum> dataQueue, InstagramUserInfoProviderConfiguration config) {
    super(instagram, dataQueue, config);
    this.config = config;
  }

  @Override
  public void run() {
    for (String userId : this.config.getInfo()) {
      try {
        collectInstagramDataForUser(userId);
      } catch (InterruptedException ie) {
        Thread.currentThread().interrupt();
      } catch (Exception ex) {
        LOGGER.error("Exception thrown while polling for user, {}, skipping user.", userId);
        LOGGER.error("Exception thrown while polling for user : ", ex);
      }
    }
    this.isCompleted.set(true);
  }

  protected void collectInstagramDataForUser(String userId) throws Exception {
    UserInfoResponse userInfoResponse = null;
    try {
      userInfoResponse = getNextInstagramClient().lookupUser(userId);
    } catch (Exception ex) {
      LOGGER.error("Expection while polling instagram : {}", ex);
    }
    if ( userInfoResponse != null && userInfoResponse.getData() != null) {
      List<UserInfo> data = new LinkedList<>();
      data.add(userInfoResponse.getData());
      super.queueData(data, userId);
    }
  }

  @Override
  protected StreamsDatum convertToStreamsDatum(UserInfo item) {
    return new StreamsDatum(item, item.getId());
  }



}
