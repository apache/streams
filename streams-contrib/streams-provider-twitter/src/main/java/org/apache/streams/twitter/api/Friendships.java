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

import org.apache.streams.twitter.pojo.Friendship;
import org.apache.streams.twitter.pojo.User;

import org.apache.juneau.http.annotation.Query;
import org.apache.juneau.remote.RemoteInterface;
import org.apache.juneau.rest.client.remote.RemoteMethod;

import java.util.List;

/**
 * Interface for /friendships methods.
 *
 * @see <a href="https://developer.twitter.com/en/docs/accounts-and-users/follow-search-get-users/overview">https://developer.twitter.com/en/docs/accounts-and-users/follow-search-get-users/overview</a>
 */
@RemoteInterface(path = "https://api.twitter.com/1.1/friendships")
public interface Friendships {

  /**
   * Allows the authenticating users to follow the user specified in the ID parameter.
   *
   * @param parameters {@link org.apache.streams.twitter.api.FriendshipCreateRequest}
   * @return {@link org.apache.streams.twitter.pojo.User}
   * @see <a href="https://developer.twitter.com/en/docs/accounts-and-users/follow-search-get-users/api-reference/post-friendships-create">https://developer.twitter.com/en/docs/accounts-and-users/follow-search-get-users/api-reference/post-friendships-create</a>
   *
   */
  @RemoteMethod(method ="POST", path = "/create.json")
  public User create(@Query(name = "*", skipIfEmpty = true) FriendshipCreateRequest parameters);

  /**
   * Allows the authenticating user to unfollow the user specified in the ID parameter.
   *
   * @param parameters {@link org.apache.streams.twitter.api.FriendshipDestroyRequest}
   * @return {@link org.apache.streams.twitter.pojo.User}
   * @see <a href="https://developer.twitter.com/en/docs/accounts-and-users/follow-search-get-users/api-reference/post-friendships-destroy">https://developer.twitter.com/en/docs/accounts-and-users/follow-search-get-users/api-reference/post-friendships-destroy</a>
   *
   */
  @RemoteMethod(method ="POST", path = "/destroy.json")
  public User destroy(@Query(name = "*", skipIfEmpty = true) FriendshipDestroyRequest parameters);

  /**
   * Returns a collection of numeric IDs for every user who has a pending request to follow the authenticating user.
   *
   * @param parameters {@link org.apache.streams.twitter.api.FriendshipsIncomingRequest}
   * @return {@link org.apache.streams.twitter.api.FollowersListResponse}
   * @see <a href="https://developer.twitter.com/en/docs/accounts-and-users/follow-search-get-users/api-reference/get-friendships-incoming">https://developer.twitter.com/en/docs/accounts-and-users/follow-search-get-users/api-reference/get-friendships-incoming</a>
   *
   */
  @RemoteMethod(method ="GET", path = "/incoming.json")
  public User incoming(@Query(name = "*", skipIfEmpty = true) FriendshipsIncomingRequest parameters);

  /**
   * Returns the relationships of the authenticating user to the comma-separated list of up to 100 screen_names or user_ids provided.
   * Values for connections can be: following, following_requested, followed_by, none, blocking, muting.
   *
   * @param parameters {@link org.apache.streams.twitter.api.FriendshipsLookupRequest}
   * @return {@link java.util.List}[{@link org.apache.streams.twitter.pojo.Friendship}]
   * @see <a href="https://developer.twitter.com/en/docs/accounts-and-users/follow-search-get-users/api-reference/get-friendships-lookup">https://developer.twitter.com/en/docs/accounts-and-users/follow-search-get-users/api-reference/get-friendships-lookup</a>
   *
   */
  @RemoteMethod(method ="GET", path = "/lookup.json")
  public List<Friendship> lookup(@Query(name = "*", skipIfEmpty = true) FriendshipsLookupRequest parameters);


  /**
   * Returns a collection of numeric IDs for every protected user for whom the authenticating user has a pending follow request.
   *
   * @param parameters {@link org.apache.streams.twitter.api.FriendshipsOutgoingRequest}
   * @return {@link org.apache.streams.twitter.api.FollowersListResponse}
   * @see <a href="https://developer.twitter.com/en/docs/accounts-and-users/follow-search-get-users/api-reference/get-friendships-incoming">https://developer.twitter.com/en/docs/accounts-and-users/follow-search-get-users/api-reference/get-friendships-incoming</a>
   *
   */
  @RemoteMethod(method ="GET", path = "/outgoing.json")
  public User outgoing(@Query(name = "*", skipIfEmpty = true) FriendshipsOutgoingRequest parameters);

  /**
   * Returns detailed information about the relationship between two arbitrary users.
   *
   * @param parameters {@link org.apache.streams.twitter.api.FriendshipShowRequest}
   * @return {@link org.apache.streams.twitter.api.FriendshipShowResponse}
   * @see <a href="https://developer.twitter.com/en/docs/accounts-and-users/follow-search-get-users/api-reference/get-friendships-show">https://developer.twitter.com/en/docs/accounts-and-users/follow-search-get-users/api-reference/get-friendships-show</a>
   *
   */
  @RemoteMethod(method ="GET", path = "/show.json")
  public FriendshipShowResponse show(@Query(name = "*", skipIfEmpty = true) FriendshipShowRequest parameters);

  /**
   * Allows one to enable or disable retweets and device notifications from the specified user.
   *
   * @param parameters {@link org.apache.streams.twitter.api.FriendshipUpdateRequest}
   * @return {@link org.apache.streams.twitter.pojo.User}
   * @see <a href="https://developer.twitter.com/en/docs/accounts-and-users/follow-search-get-users/api-reference/post-friendships-update">https://developer.twitter.com/en/docs/accounts-and-users/follow-search-get-users/api-reference/post-friendships-update</a>
   *
   */
  @RemoteMethod(method ="GET", path = "/update.json")
  public User update(@Query(name = "*", skipIfEmpty = true) FriendshipUpdateRequest parameters);

}
