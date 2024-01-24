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

import org.apache.juneau.http.annotation.Query;
import org.apache.juneau.http.remote.Remote;
import org.apache.juneau.http.remote.RemoteGet;

import java.util.List;

/**
 * Interface for /favorites methods.
 *
 * @see <a href="https://dev.twitter.com/rest/reference">https://dev.twitter.com/rest/reference</a>
 */
@Remote(path = "https://api.twitter.com/1.1/favorites")
public interface Favorites {

  /**
   * Returns the 20 most recent Tweets favorited by the authenticating or specified user.
   * Bind this at: /favorites
   *
   * @param parameters {@link org.apache.streams.twitter.api.FavoritesListRequest}
   * @return List < Tweet >
   * @see <a href="https://dev.twitter.com/rest/reference/get/favorites/list">https://dev.twitter.com/rest/reference/get/favorites/list</a>
   *
   */
  @RemoteGet(path = "/list.json")
  public List<Tweet> list( @Query(name = "*") FavoritesListRequest parameters);

}
