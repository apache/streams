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
@RemoteInterface(path = "https://api.twitter.com/1.1/"+ ThirtyDaySearch.path)
public interface ThirtyDaySearchCounts {

  /**
   * Returns counts of relevant Tweets matching a specified query.
   *
   * @param environment "environment to use"
   * @param searchCountsRequest {@link org.apache.streams.twitter.api.ThirtyDaySearchCountsRequest}
   * @return {@link ThirtyDaySearchCountsResponse}
   * @see <a href=https://developer.twitter.com/en/docs/tweets/search/api-reference/30-day-search">https://developer.twitter.com/en/docs/tweets/search/api-reference/30-day-search</a>
   *
   */
  @RemoteMethod(method ="POST", path = "/{environment}/counts.json")
  public ThirtyDaySearchCountsResponse thirtyDaySearchCounts(@Path("environment") String environment, @Body ThirtyDaySearchCountsRequest searchCountsRequest);

}
