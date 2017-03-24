package org.apache.streams.twitter.api;

import org.apache.streams.twitter.pojo.Tweet;
import org.apache.streams.twitter.pojo.User;

import java.util.List;

/**
 * Returns a collection of the most recent Tweets posted by the user indicated by the screen_name or user_id parameters.
 *
 * @see <a href="https://dev.twitter.com/rest/reference/get/statuses/user_timeline">https://api.twitter.com/1.1/statuses/user_timeline.json</a>
 */
public interface Users {

  /**
   * Returns a variety of information about the user specified by the required user_id or screen_name parameter. The author’s most recent Tweet will be returned inline when possible.
   *
   * @param parameters UsersLookupRequest
   * @return List<Tweet>
   * @see <a href="https://api.twitter.com/1.1/users/lookup.json">https://api.twitter.com/1.1/users/lookup.json</a>
   *
   */
  public List<User> lookup(UsersLookupRequest parameters);

  /**
   * Returns a single Tweet, specified by the id parameter. The Tweet’s author will also be embedded within the Tweet.
   *
   * @param parameters UsersShowRequest
   * @return List<Tweet>
   * @see <a href="https://api.twitter.com/1.1/users/show.json">https://api.twitter.com/1.1/users/show.json</a>
   *
   */
  public User show(UsersShowRequest parameters);

}
