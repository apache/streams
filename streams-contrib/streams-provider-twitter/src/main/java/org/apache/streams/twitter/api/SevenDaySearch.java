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

import org.apache.juneau.remoteable.Body;
import org.apache.juneau.remoteable.QueryIfNE;
import org.apache.juneau.remoteable.RemoteMethod;
import org.apache.juneau.remoteable.Remoteable;

/**
 * Interface for /search methods.
 */
@Remoteable(path = "https://api.twitter.com/1.1/search")
public interface SevenDaySearch {

  /**
   * Returns a collection of relevant Tweets matching a specified query.
   *
   * @return {@link AccountSettings}
   * @see <a href=https://developer.twitter.com/en/docs/accounts-and-users/manage-account-settings/api-reference/get-account-settings">https://developer.twitter.com/en/docs/accounts-and-users/manage-account-settings/api-reference/get-account-settings</a>
   *
   */
  @RemoteMethod(httpMethod = "GET", path = "/tweets.json")
  public SevenDaySearchResponse sevenDaySearch(@QueryIfNE SevenDaySearchRequest parameters);

}

