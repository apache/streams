package org.apache.streams.twitter.api;

import org.apache.streams.twitter.pojo.Tweet;

import java.util.List;

/**
 * Returns a collection of the most recent Tweets posted by the user indicated by the screen_name or user_id parameters.
 *
 * @see <a href="https://dev.twitter.com/rest/reference/get/statuses/user_timeline">https://api.twitter.com/1.1/statuses/user_timeline.json</a>
 */
public interface Statuses {

  /**
   * Returns a single Tweet, specified by the id parameter. The Tweet’s author will also be embedded within the Tweet.
   *
   * @param parameters StatusesLookupRequest
   * @return List<Tweet>
   * @see <a href="https://api.twitter.com/1.1/statuses/show.json">https://api.twitter.com/1.1/statuses/show.json</a>
   *
   */
  public List<Tweet> lookup(StatusesLookupRequest parameters);

  /**
   * Returns a single Tweet, specified by the id parameter. The Tweet’s author will also be embedded within the Tweet.
   *
   * @param parameters StatusesUserTimelineRequest
   * @return List<Tweet>
   * @see <a href="https://api.twitter.com/1.1/statuses/show.json">https://api.twitter.com/1.1/statuses/show.json</a>
   *
   */
  public Tweet show(StatusesShowRequest parameters);

  /**
   * Returns a collection of the most recent Tweets posted by the user indicated by the screen_name or user_id parameters.
   *
   * @param parameters StatusesUserTimelineRequest
   * @return List<Tweet>
   * @see <a href="https://api.twitter.com/1.1/statuses/user_timeline.json">https://api.twitter.com/1.1/statuses/user_timeline.json</a>
   *
   */
  public List<Tweet> userTimeline(StatusesUserTimelineRequest parameters);

}
