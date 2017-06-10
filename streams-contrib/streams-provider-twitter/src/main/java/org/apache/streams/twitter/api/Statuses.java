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

import org.apache.juneau.remoteable.Body;
import org.apache.juneau.remoteable.Path;
import org.apache.juneau.remoteable.Query;
import org.apache.juneau.remoteable.QueryIfNE;
import org.apache.juneau.remoteable.RemoteMethod;
import org.apache.juneau.remoteable.Remoteable;
import org.apache.juneau.remoteable.RequestBean;

import java.util.List;

/**
 * Interface for /statuses methods.
 *
 * @see <a href="https://dev.twitter.com/rest/reference">https://dev.twitter.com/rest/reference</a>
 */
@Remoteable(path = "https://api.twitter.com/1.1/statuses")
public interface Statuses {

  /**
   * Returns a collection of the most recent Tweets posted by the user indicated by the screen_name or user_id parameters.
   *
   * @param parameters {@link org.apache.streams.twitter.api.StatusesHomeTimelineRequest}
   * @return List<Tweet>
   * @see <a href="https://dev.twitter.com/rest/reference/get/statuses/home_timeline">https://dev.twitter.com/rest/reference/get/statuses/home_timeline</a>
   *
   */
  @RemoteMethod(httpMethod = "GET", path = "/home_timeline.json")
  public List<Tweet> homeTimeline( @QueryIfNE("*") StatusesHomeTimelineRequest parameters );

  /**
   * Returns fully-hydrated Tweet objects for up to 100 Tweets per request, as specified by comma-separated values passed to the id parameter.
   *
   * @param parameters {@link org.apache.streams.twitter.api.StatusesLookupRequest}
   * @return List<Tweet>
   * @see <a href="https://dev.twitter.com/rest/reference/get/statuses/lookup">https://dev.twitter.com/rest/reference/get/statuses/lookup</a>
   *
   */
  @RemoteMethod(httpMethod = "GET", path = "/lookup.json")
  public List<Tweet> lookup( @QueryIfNE("*") StatusesLookupRequest parameters);

  /**
   * Returns the 20 most recent mentions (Tweets containing a users’s @screen_name) for the authenticating user.
   *
   * The timeline returned is the equivalent of the one seen when you view your mentions on twitter.com.
   *
   * This method can only return up to 800 tweets.
   *
   * @param parameters {@link org.apache.streams.twitter.api.StatusesUserTimelineRequest}
   * @return List<Tweet>
   * @see <a href="https://dev.twitter.com/rest/reference/get/statuses/mentions_timeline">https://dev.twitter.com/rest/reference/get/statuses/mentions_timeline</a>
   *
   */
  @RemoteMethod(httpMethod = "GET", path = "/mentions_timeline.json")
  public List<Tweet> mentionsTimeline( @QueryIfNE("*") StatusesMentionsTimelineRequest parameters);

  /**
   * Returns a single Tweet, specified by the id parameter. The Tweet’s author will also be embedded within the Tweet.
   *
   * @param parameters {@link org.apache.streams.twitter.api.StatusesShowRequest}
   * @return List<Tweet>
   * @see <a href="https://dev.twitter.com/rest/reference/get/statuses/show/id">https://dev.twitter.com/rest/reference/get/statuses/show/id</a>
   *
   */
  @RemoteMethod(httpMethod = "GET", path = "/show/{id}")
  public Tweet show( @RequestBean StatusesShowRequest parameters);

  /**
   * Returns a collection of the most recent Tweets posted by the user indicated by the screen_name or user_id parameters.
   *
   * @param parameters {@link org.apache.streams.twitter.api.StatusesUserTimelineRequest}
   * @return List<Tweet>
   * @see <a href="https://dev.twitter.com/rest/reference/get/statuses/user_timeline">https://dev.twitter.com/rest/reference/get/statuses/user_timeline</a>
   *
   */
  @RemoteMethod(httpMethod = "GET", path = "/user_timeline.json")
  public List<Tweet> userTimeline( @QueryIfNE("*") StatusesUserTimelineRequest parameters);

  interface StatusesShowRequestAnnotations {

    @Path("id")
    Long getId();

  }
}
