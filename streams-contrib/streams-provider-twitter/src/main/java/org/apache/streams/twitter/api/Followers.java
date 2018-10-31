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

import org.apache.juneau.http.annotation.Query;
import org.apache.juneau.remote.RemoteInterface;
import org.apache.juneau.rest.client.remote.RemoteMethod;

/**
 * Interface for /followers methods.
 *
 * @see <a href="https://developer.twitter.com/en/docs/accounts-and-users/follow-search-get-users/api-reference">https://developer.twitter.com/en/docs/accounts-and-users/follow-search-get-users/api-reference</a>
 */
@RemoteInterface(path = "https://api.twitter.com/1.1/followers")
public interface Followers {

  /**
   * Returns a cursored collection of user IDs for every user following the specified user.
   *
   * @param parameters {@link org.apache.streams.twitter.api.FollowersIdsRequest}
   * @return FollowersIdsResponse
   * @see <a href="https://developer.twitter.com/en/docs/accounts-and-users/follow-search-get-users/api-reference/get-followers-ids">https://developer.twitter.com/en/docs/accounts-and-users/follow-search-get-users/api-reference/get-followers-ids</a>
   *
   */
  @RemoteMethod(method ="GET", path = "/ids.json")
  public FollowersIdsResponse ids( @Query(name = "*", skipIfEmpty = true) FollowersIdsRequest parameters);

  /**
   * Returns a cursored collection of user objects for users following the specified user.
   *
   * @param parameters {@link org.apache.streams.twitter.api.FollowersListRequest}
   * @return FollowersListResponse
   * @see <a href="https://developer.twitter.com/en/docs/accounts-and-users/follow-search-get-users/api-reference/get-followers-list">https://developer.twitter.com/en/docs/accounts-and-users/follow-search-get-users/api-reference/get-followers-list</a>
   *
   */
  @RemoteMethod(method ="GET", path = "/list.json")
  public FollowersListResponse list( @Query(name = "*", skipIfEmpty = true) FollowersListRequest parameters);

}
