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

import org.apache.juneau.http.annotation.Body;
import org.apache.juneau.http.annotation.Path;
import org.apache.juneau.remote.RemoteInterface;
import org.apache.juneau.rest.client.remote.RemoteMethod;

/**
 * Interface for /search methods.
 */
@RemoteInterface(path = "https://api.twitter.com/1.1/"+ThirtyDaySearch.path)
public interface ThirtyDaySearch {

  String path = "tweets/search/30day";

  /**
   * Returns a collection of relevant Tweets matching a specified query.
   *
   * @param environment "environment to use"
   * @param searchRequest {@link org.apache.streams.twitter.api.ThirtyDaySearchRequest}
   * @return {@link ThirtyDaySearchResponse}
   * @see <a href=https://developer.twitter.com/en/docs/tweets/search/overview/premium">https://developer.twitter.com/en/docs/tweets/search/overview/premium</a>
   *
   */
  @RemoteMethod(method ="POST", path = "/{environment}.json")
  public ThirtyDaySearchResponse thirtyDaySearch(@Path("environment") String environment, @Body ThirtyDaySearchRequest searchRequest);

}
