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

import org.apache.streams.twitter.pojo.User;

import org.apache.juneau.http.annotation.Path;
import org.apache.juneau.http.annotation.Query;
import org.apache.juneau.http.remote.Remote;
import org.apache.juneau.http.remote.RemoteGet;

import java.util.List;

/**
 * Interface for /users methods.
 *
 * @see <a href="https://dev.twitter.com/rest/reference">https://dev.twitter.com/rest/reference</a>
 */
@Remote(path = "https://api.twitter.com/1.1/users")
public interface SuggestedUsers {

  /**
   * Access to Twitter’s suggested user list. This returns the list of suggested user categories.
   *
   * @param lang Restricts the suggested categories to the requested language. The language must be specified by the appropriate two letter ISO 639-1 representation.
   * @return {@link java.util.List}[{@link SuggestedUserCategory}]
   * @see <a href="https://developer.twitter.com/en/docs/accounts-and-users/follow-search-get-users/api-reference/get-users-suggestions">https://developer.twitter.com/en/docs/accounts-and-users/follow-search-get-users/api-reference/get-users-suggestions</a>
   *
   */
  @RemoteGet(path = "/suggestions.json")
  public List<SuggestedUserCategory> categories(@Query(name = "lang") String lang);

  /**
   * Access the users in a given category of the Twitter suggested user list.
   *
   * It is recommended that applications cache this data for no more than one hour.
   *
   * @return {@link SuggestedUserCategory}
   * @see <a href="https://developer.twitter.com/en/docs/accounts-and-users/follow-search-get-users/api-reference/get-users-suggestions-slug">https://developer.twitter.com/en/docs/accounts-and-users/follow-search-get-users/api-reference/get-users-suggestions-slug</a>
   *
   */
  @RemoteGet(path = "/suggestions/{slug}.json")
  public SuggestedUserCategory suggestions(@Path("slug") String slug, @Query(name = "lang") String lang);

  /**
   * Access the users in a given category of the Twitter suggested user list and return their most recent status if they are not a protected user.
   *
   * @return {@link java.util.List}[{@link org.apache.streams.twitter.pojo.User}]
   * @see <a href="https://developer.twitter.com/en/docs/accounts-and-users/follow-search-get-users/api-reference/get-users-suggestions-slug">https://developer.twitter.com/en/docs/accounts-and-users/follow-search-get-users/api-reference/get-users-suggestions-slug</a>
   *
   */
  @RemoteGet(path = "/suggestions/{slug}/members.json")
  public List<User> members(@Path("slug") String slug);

}
