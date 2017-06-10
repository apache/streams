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

import org.apache.juneau.remoteable.Path;
import org.apache.juneau.remoteable.RemoteMethod;
import org.apache.juneau.remoteable.Remoteable;

/**
 * Relationship Endpoints.
 *
 * @see <a href="https://www.instagram.com/developer/endpoints/relationships/">https://www.instagram.com/developer/endpoints/relationships/</a>
 */
@Remoteable(path = "/users")
public interface Relationships {

  /**
   * Get the list of users this user follows.
   *
   * @see <a href="https://www.instagram.com/developer/endpoints/relationships/#get_users_follows">https://www.instagram.com/developer/endpoints/relationships/#get_users_follows</a>
   * @return SearchUsersResponse @link{SearchUsersResponse}
   */
  @RemoteMethod(httpMethod = "GET", path = "/self/follows")
  SearchUsersResponse follows();

  /**
   * Get the list of users this user is followed by.
   *
   * @see <a href="https://www.instagram.com/developer/endpoints/relationships/#get_users_followed_by">https://www.instagram.com/developer/endpoints/relationships/#get_users_followed_by</a>
   * @return SearchUsersResponse @link{SearchUsersResponse}
   */
  @RemoteMethod(httpMethod = "GET", path = "/self/followed-by")
  SearchUsersResponse followedBy();

  /**
   * List the users who have requested this user's permission to follow.
   *
   * @see <a href="https://www.instagram.com/developer/endpoints/relationships/#get_incoming_requests">https://www.instagram.com/developer/endpoints/relationships/#get_incoming_requests</a>
   * @return SearchUsersResponse @link{SearchUsersResponse}
   */
  @RemoteMethod(httpMethod = "GET", path = "/self/requested-by")
  SearchUsersResponse requestedBy();

  /**
   * Get information about a relationship to another user. Relationships are expressed using the following terms in the response:
   *   outgoing_status: Your relationship to the user. Can be 'follows', 'requested', 'none'.
   *   incoming_status: A user's relationship to you. Can be 'followed_by', 'requested_by', 'blocked_by_you', 'none'.
   *
   * @see <a href="https://www.instagram.com/developer/endpoints/relationships/#get_relationship">https://www.instagram.com/developer/endpoints/relationships/#get_relationship</a>
   * @return SearchUsersResponse @link{SearchUsersResponse}
   */
  @RemoteMethod(httpMethod = "GET", path = "/{user_id}/relationship")
  RelationshipResponse relationship( @Path("user_id") Long user_id);
}
