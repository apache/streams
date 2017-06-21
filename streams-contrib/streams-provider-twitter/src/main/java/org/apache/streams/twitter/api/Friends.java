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

import org.apache.juneau.remoteable.QueryIfNE;
import org.apache.juneau.remoteable.RemoteMethod;
import org.apache.juneau.remoteable.Remoteable;

import java.util.List;

/**
 * Interface for /friends methods.
 *
 * @see <a href="https://dev.twitter.com/rest/reference">https://dev.twitter.com/rest/reference</a>
 */
@Remoteable(path = "https://api.twitter.com/1.1/friends")
public interface Friends {

  /**
   * Returns a cursored collection of user IDs for every user the specified user is following.
   *
   * @param parameters {@link org.apache.streams.twitter.api.FriendsIdsRequest}
   * @return FriendsIdsResponse
   * @see <a href="https://dev.twitter.com/rest/reference/get/friends/ids">https://dev.twitter.com/rest/reference/get/friends/ids</a>
   *
   */
  @RemoteMethod(httpMethod = "GET", path = "/ids.json")
  public FriendsIdsResponse ids( @QueryIfNE("*") FriendsIdsRequest parameters);

  /**
   * Returns a cursored collection of user objects for every user the specified user is following.
   *
   * @param parameters {@link org.apache.streams.twitter.api.FriendsListRequest}
   * @return FriendsListResponse
   * @see <a href="https://dev.twitter.com/rest/reference/get/friends/list">https://dev.twitter.com/rest/reference/get/friends/list</a>
   *
   */
  @RemoteMethod(httpMethod = "GET", path = "/list.json")
  public FriendsListResponse list( @QueryIfNE("*") FriendsListRequest parameters);

}
