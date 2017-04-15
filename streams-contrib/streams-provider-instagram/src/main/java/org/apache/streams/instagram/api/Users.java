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

package org.apache.streams.instagram.api;

import org.apache.streams.instagram.pojo.UserInfo;
import org.apache.streams.instagram.pojo.UserRecentMediaRequest;

/**
 * User Endpoints.
 *
 * @see <a href="https://www.instagram.com/developer/endpoints/users/">https://www.instagram.com/developer/endpoints/users/</a>
 */
public interface Users {

  /**
   * Get information about the owner of the access_token.
   *
   * @see <a href="https://www.instagram.com/developer/endpoints/users/#get_users_self">https://www.instagram.com/developer/endpoints/users/#get_users_self</a>
   * @return UserInfoResponse @link{org.apache.streams.instagram.api.UserInfoResponse}
   */
  public UserInfoResponse self();

  /**
   * Get information about a user.
   * The public_content scope is required if the user is not the owner of the access_token.
   *
   * @see <a href="https://www.instagram.com/developer/endpoints/users/#get_users">https://www.instagram.com/developer/endpoints/users/#get_users</a>
   * @param user_id user_id
   * @return UserInfoResponse @link{org.apache.streams.instagram.api.UserInfoResponse}
   */
  public UserInfoResponse lookupUser(String user_id);

  /**
   * Get the most recent media published by the owner of the access_token.
   *
   * @see <a href="https://www.instagram.com/developer/endpoints/users/#get_users_media_recent_self">https://www.instagram.com/developer/endpoints/users/#get_users_media_recent_self</a>
   * @param parameters @link{org.apache.streams.instagram.api.SelfRecentMediaRequest}
   * @return RecentMediaResponse @link{org.apache.streams.instagram.api.RecentMediaResponse}
   */
  public RecentMediaResponse selfMediaRecent(SelfRecentMediaRequest parameters);

  /**
   * Get the most recent media published by a user.
   * The public_content scope is required if the user is not the owner of the access_token.
   *
   * @see <a href="https://www.instagram.com/developer/endpoints/users/#get_users_media_recent">https://www.instagram.com/developer/endpoints/users/#get_users_media_recent</a>
   * @param parameters @link{org.apache.streams.instagram.api.UserRecentMediaRequest}
   * @return RecentMediaResponse @link{org.apache.streams.instagram.api.RecentMediaResponse}
   */
  public RecentMediaResponse userMediaRecent(UserRecentMediaRequest parameters);

  /**
   * Get the list of recent media liked by the owner of the access_token.
   *
   * @see <a href="https://www.instagram.com/developer/endpoints/users/#get_users_feed_liked">https://www.instagram.com/developer/endpoints/users/#get_users_feed_liked</a>
   * @param parameters @link{org.apache.streams.instagram.api.SelfLikedMediaRequest}
   * @return RecentMediaResponse @link{org.apache.streams.instagram.api.RecentMediaResponse}
   */
  public RecentMediaResponse selfMediaLiked(SelfLikedMediaRequest parameters);

  /**
   * Get a list of users matching the query.
   *
   * @see <a href="https://www.instagram.com/developer/endpoints/users/#get_users_search">https://www.instagram.com/developer/endpoints/users/#get_users_search</a>
   * @param parameters @link{org.apache.streams.instagram.api.SearchUsersRequest}
   * @return SearchUsersResponse @link{org.apache.streams.instagram.api.SearchUsersResponse}
   */
  public SearchUsersResponse searchUser(SearchUsersRequest parameters);
}
