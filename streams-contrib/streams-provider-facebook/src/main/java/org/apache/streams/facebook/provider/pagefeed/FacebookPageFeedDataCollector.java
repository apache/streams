/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
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

package org.apache.streams.facebook.provider.pagefeed;

import org.apache.streams.core.StreamsDatum;
import org.apache.streams.facebook.FacebookConfiguration;
import org.apache.streams.facebook.IdConfig;
import org.apache.streams.facebook.provider.FacebookDataCollector;
import org.apache.streams.jackson.StreamsJacksonMapper;

import com.fasterxml.jackson.databind.ObjectMapper;
import facebook4j.FacebookException;
import facebook4j.Paging;
import facebook4j.Post;
import facebook4j.Reading;
import facebook4j.ResponseList;
import facebook4j.json.DataObjectFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.BlockingQueue;

/**
 * Collects the page feed data from public Facebook pages.
 */
public class FacebookPageFeedDataCollector extends FacebookDataCollector {

  private static final Logger LOGGER = LoggerFactory.getLogger(FacebookPageFeedDataCollector.class);
  private static final int MAX_ATTEMPTS = 5;
  private static final ObjectMapper MAPPER = StreamsJacksonMapper.getInstance();
  private static final int LIMIT = 100;

  public FacebookPageFeedDataCollector(BlockingQueue<StreamsDatum> queue, FacebookConfiguration configuration) {
    super(configuration, queue);
  }

  @Override
  protected void getData(IdConfig id) throws Exception {
    boolean exit = false;

    ResponseList<Post> facebookPosts = getPosts(id.getId());
    LOGGER.debug("Post received : {}", facebookPosts.size());
    backOff.reset();
    do {
      for (Post post : facebookPosts) {
        if (id.getBeforeDate() != null && id.getAfterDate() != null) {
          if (id.getBeforeDate().isAfter(post.getCreatedTime().getTime())
              && id.getAfterDate().isBefore(post.getCreatedTime().getTime())) {
            super.outputData(MAPPER.readValue(DataObjectFactory.getRawJSON(post), org.apache.streams.facebook.Post.class), post.getId());

          }
        } else if (id.getBeforeDate() != null && id.getBeforeDate().isAfter(post.getCreatedTime().getTime())) {
          super.outputData(MAPPER.readValue(DataObjectFactory.getRawJSON(post), org.apache.streams.facebook.Post.class), post.getId());
        } else if (id.getAfterDate() != null && id.getAfterDate().isBefore(post.getCreatedTime().getTime())) {
          super.outputData(MAPPER.readValue(DataObjectFactory.getRawJSON(post), org.apache.streams.facebook.Post.class), post.getId());
        } else if (id.getBeforeDate() == null && id.getAfterDate() == null) {
          super.outputData(MAPPER.readValue(DataObjectFactory.getRawJSON(post), org.apache.streams.facebook.Post.class), post.getId());
        } else {
          exit = true;
          LOGGER.debug("Breaking on post, {}, with createdAtDate {}", post.getId(), post.getCreatedTime());
          break;
        }
      }
      if (facebookPosts.getPaging() != null && !exit) {
        LOGGER.debug("Paging. . .");
        facebookPosts = getPosts(facebookPosts.getPaging());
        backOff.reset();
        LOGGER.debug("Paging received {} posts*", facebookPosts.size());
      } else {
        LOGGER.debug("No more paging.");
        facebookPosts = null;
      }
    }
    while (facebookPosts != null && facebookPosts.size() != 0);

  }

  private ResponseList<Post> getPosts(Paging<Post> paging) throws Exception {
    return getPosts(null, paging);
  }

  private ResponseList<Post> getPosts(String pageId) throws Exception {
    return getPosts(pageId, null);
  }

  /**
   * Queries facebook.  Attempts requests up to 5 times and backs off on each facebook exception.
   * @param pageId pageId
   * @param paging paging
   * @return ResponseList of $link{facebook4j.Post}
   * @throws Exception Exception
   */
  private ResponseList<Post> getPosts(String pageId, Paging<Post> paging) throws Exception {
    int attempt = 0;
    while (attempt < MAX_ATTEMPTS) {
      ++attempt;
      try {
        if (pageId != null) {
          Reading reading = new Reading();
          reading.limit(LIMIT);
          return getNextFacebookClient().getPosts(pageId, reading);
        } else {
          return getNextFacebookClient().fetchNext(paging);
        }
      } catch (FacebookException fe) {
        LOGGER.error("Facebook returned an exception : {}", fe);
        LOGGER.error("Facebook returned an exception while trying to get feed for page, {} : {}", pageId, fe.getMessage());
        //TODO Rate limit exceptions with facebook4j unclear http://facebook4j.org/oldjavadocs/1.1.12-2.0.0/2.0.0/index.html?facebook4j/internal/http/HttpResponseCode.html
        // back off at all exceptions until figured out.
        int errorCode = fe.getErrorCode();

        //Some sort of rate limiting
        if (errorCode == 17 || errorCode == 4 || errorCode == 341) {
          super.backOff.backOff();
        }
      }
    }
    throw new Exception("Failed to get data from facebook after " + MAX_ATTEMPTS);
  }
}
