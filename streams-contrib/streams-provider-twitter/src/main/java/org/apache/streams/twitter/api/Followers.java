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

package org.apache.streams.twitter.api;

import org.apache.streams.twitter.pojo.Tweet;

import java.util.List;

/**
 * Returns a collection of the most recent Tweets posted by the user indicated by the screen_name or user_id parameters.
 *
 * @see <a href="https://dev.twitter.com/rest/reference/get/statuses/user_timeline">https://api.twitter.com/1.1/statuses/user_timeline.json</a>
 */
public interface Followers {

  /**
   * Returns a cursored collection of user IDs for every user following the specified user.
   *
   * @param parameters StatusesLookupRequest
   * @return List < Tweet >
   * @see <a href="https://api.twitter.com/1.1/followers/ids.json">https://api.twitter.com/1.1/followers/ids.json</a>
   *
   */
  public FollowersIdsResponse ids(FollowersIdsRequest parameters);

  /**
   * Returns a cursored collection of user objects for users following the specified user.
   *
   * @param parameters StatusesUserTimelineRequest
   * @return List < Tweet >
   * @see <a href="https://api.twitter.com/1.1/followers/list.json">https://api.twitter.com/1.1/followers/list.json</a>
   *
   */
  public FollowersListResponse list(FollowersListRequest parameters);

}
