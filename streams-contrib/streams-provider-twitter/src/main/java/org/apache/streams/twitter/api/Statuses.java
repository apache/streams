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

import org.apache.juneau.http.annotation.Path;
import org.apache.juneau.http.annotation.Query;
import org.apache.juneau.http.annotation.Request;
import org.apache.juneau.http.remote.Remote;
import org.apache.juneau.http.remote.RemoteGet;

import java.util.List;

/**
 * Interface for /statuses methods.
 *
 * @see <a href="https://developer.twitter.com/en/docs/tweets/post-and-engage/overview">https://developer.twitter.com/en/docs/tweets/post-and-engage/overview</a>
 * @see <a href="https://developer.twitter.com/en/docs/tweets/timelines/overview">https://developer.twitter.com/en/docs/tweets/timelines/overview</a>
 */
@Remote(path = "https://api.twitter.com/1.1/statuses")
public interface Statuses {

  /**
   * Returns a collection of the most recent Tweets posted by the user indicated by the screen_name or user_id parameters.
   *
   * @param parameters {@link org.apache.streams.twitter.api.StatusesHomeTimelineRequest}
   * @return {@link java.util.List}[{@link org.apache.streams.twitter.pojo.Tweet}]
   * @see <a href="https://developer.twitter.com/en/docs/tweets/timelines/api-reference/get-statuses-home_timeline">https://developer.twitter.com/en/docs/tweets/timelines/api-reference/get-statuses-home_timeline</a>
   *
   */
  @RemoteGet(path = "/home_timeline.json")
  public List<Tweet> homeTimeline( @Query(name = "*") StatusesHomeTimelineRequest parameters );

  /**
   * Returns fully-hydrated Tweet objects for up to 100 Tweets per request, as specified by comma-separated values passed to the id parameter.
   *
   * @param parameters {@link org.apache.streams.twitter.api.StatusesLookupRequest}
   * @return {@link java.util.List}[{@link org.apache.streams.twitter.pojo.Tweet}]
   * @see <a href="https://developer.twitter.com/en/docs/tweets/post-and-engage/api-reference/get-statuses-lookup">https://developer.twitter.com/en/docs/tweets/post-and-engage/api-reference/get-statuses-lookup</a>
   *
   */
  @RemoteGet(path = "/lookup.json")
  public List<Tweet> lookup( @Query(name = "*") StatusesLookupRequest parameters);

  /**
   * Returns the 20 most recent mentions (Tweets containing a users’s @screen_name) for the authenticating user.
   *
   * The timeline returned is the equivalent of the one seen when you view your mentions on twitter.com.
   *
   * This method can only return up to 800 tweets.
   *
   * @param parameters {@link org.apache.streams.twitter.api.StatusesMentionsTimelineRequest}
   * @return {@link java.util.List}[{@link org.apache.streams.twitter.pojo.Tweet}]
   * @see <a href="https://developer.twitter.com/en/docs/tweets/timelines/api-reference/get-statuses-mentions_timeline">https://developer.twitter.com/en/docs/tweets/timelines/api-reference/get-statuses-mentions_timeline</a>
   *
   */
  @RemoteGet(path = "/mentions_timeline.json")
  public List<Tweet> mentionsTimeline( @Query(name = "*") StatusesMentionsTimelineRequest parameters);

  /**
   * Returns a single Tweet, specified by the id parameter. The Tweet’s author will also be embedded within the Tweet.
   *
   * @param parameters {@link org.apache.streams.twitter.api.StatusesShowRequest}
   * @return {@link org.apache.streams.twitter.pojo.Tweet}
   * @see <a href="https://developer.twitter.com/en/docs/tweets/post-and-engage/api-reference/get-statuses-show-id">https://developer.twitter.com/en/docs/tweets/post-and-engage/api-reference/get-statuses-show-id</a>
   *
   */
  @RemoteGet(path = "/show/{id}")
  public Tweet show( @Request StatusesShowRequest parameters);

  /**
   * Returns a collection of the most recent Tweets posted by the user indicated by the screen_name or user_id parameters.
   *
   * @param parameters {@link org.apache.streams.twitter.api.StatusesUserTimelineRequest}
   * @return {@link java.util.List}[{@link org.apache.streams.twitter.pojo.Tweet}]
   * @see <a href="https://developer.twitter.com/en/docs/tweets/timelines/api-reference/get-statuses-user_timeline">https://developer.twitter.com/en/docs/tweets/timelines/api-reference/get-statuses-user_timeline</a>
   *
   */
  @RemoteGet(path = "/user_timeline.json")
  public List<Tweet> userTimeline( @Query(name = "*") StatusesUserTimelineRequest parameters);

  /**
   * Returns a collection of the 100 most recent retweets of the Tweet specified by the id parameter.
   *
   * @param parameters {@link org.apache.streams.twitter.api.RetweetsRequest}
   * @return {@link java.util.List}[{@link org.apache.streams.twitter.pojo.Tweet}]
   * @see <a href="https://developer.twitter.com/en/docs/tweets/post-and-engage/api-reference/get-statuses-retweets-id">https://developer.twitter.com/en/docs/tweets/post-and-engage/api-reference/get-statuses-retweets-id</a>
   *
   */
  @RemoteGet(path = "/retweets/{id}")
  public List<Tweet> retweets( @Request RetweetsRequest parameters);

  /**
   * Returns a collection of up to 100 user IDs belonging to users who have retweeted the Tweet specified by the id parameter.
   *
   * @param parameters {@link org.apache.streams.twitter.api.RetweeterIdsRequest}
   * @return {@link org.apache.streams.twitter.api.RetweeterIdsResponse}
   * @see <a href="https://developer.twitter.com/en/docs/tweets/post-and-engage/api-reference/get-statuses-retweeters-ids">https://developer.twitter.com/en/docs/tweets/post-and-engage/api-reference/get-statuses-retweeters-ids</a>
   *
   */
  @RemoteGet(path = "/retweeters/ids.json")
  public RetweeterIdsResponse retweeterIds( @Query(name = "*") RetweeterIdsRequest parameters);

  interface StatusesShowRequestAnnotations {

    @Path("id")
    Long getId();

  }

  interface RetweetsRequestAnnotations {

    @Path("id")
    Long getId();

  }
}
