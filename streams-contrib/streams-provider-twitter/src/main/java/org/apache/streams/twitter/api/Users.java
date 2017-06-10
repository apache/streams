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
import org.apache.streams.twitter.pojo.User;

import org.apache.juneau.remoteable.Path;
import org.apache.juneau.remoteable.QueryIfNE;
import org.apache.juneau.remoteable.RemoteMethod;
import org.apache.juneau.remoteable.Remoteable;
import org.apache.juneau.remoteable.RequestBean;

import java.util.List;

/**
 * Interface for /users methods.
 *
 * @see <a href="https://dev.twitter.com/rest/reference">https://dev.twitter.com/rest/reference</a>
 */
@Remoteable(path = "https://api.twitter.com/1.1/users")
public interface Users {

  /**
   * Returns fully-hydrated user objects for up to 100 users per request, as specified by comma-separated values passed to the user_id and/or screen_name parameters.
   *
   * @param parameters {@link org.apache.streams.twitter.api.UsersLookupRequest}
   * @return List<Tweet>
   * @see <a href="https://dev.twitter.com/rest/reference/get/users/lookup">https://dev.twitter.com/rest/reference/get/users/lookup</a>
   *
   */
  @RemoteMethod(httpMethod = "GET", path = "/lookup.json")
  public List<User> lookup( @QueryIfNE UsersLookupRequest parameters);

  /**
   * Returns a variety of information about the user specified by the required user_id or screen_name parameter. The author’s most recent Tweet will be returned inline when possible.
   *
   * @param parameters {@link org.apache.streams.twitter.api.UsersShowRequest}
   * @return List<Tweet>
   * @see <a href="https://dev.twitter.com/rest/reference/get/users/show">https://dev.twitter.com/rest/reference/get/users/show</a>
   *
   */
  @RemoteMethod(httpMethod = "GET", path = "/show.json")
  public User show( @QueryIfNE UsersShowRequest parameters);

}
