package org.apache.streams.twitter.api;

import org.apache.streams.twitter.pojo.WelcomeMessage;

import org.apache.juneau.remoteable.Body;
import org.apache.juneau.remoteable.Query;
import org.apache.juneau.remoteable.QueryIfNE;
import org.apache.juneau.remoteable.RemoteMethod;
import org.apache.juneau.remoteable.Remoteable;
import org.apache.juneau.remoteable.RequestBean;

/**
 * Interface for /direct_messages/welcome_messages methods.
 *
 * @see <a href="https://dev.twitter.com/rest/reference">https://dev.twitter.com/rest/reference</a>
 */
@Remoteable(path = "https://api.twitter.com/1.1/direct_messages/welcome_messages")
public interface WelcomeMessages {

  /**
   * Returns a list of Welcome Messages.
   *
   * @return WelcomeMessageListResponse
   * @see <a href="https://dev.twitter.com/rest/reference/get/direct_messages/welcome_messages/list">https://dev.twitter.com/rest/reference/get/direct_messages/welcome_messages/list</a>
   *
   */
  @RemoteMethod(httpMethod = "GET", path = "/list.json")
  public WelcomeMessagesListResponse listWelcomeMessages(@QueryIfNE("*") WelcomeMessagesListRequest parameters);

  /**
   * Returns a Welcome Message by the given id.
   *
   * @return WelcomeMessage
   * @see <a href="https://dev.twitter.com/rest/reference/get/direct_messages/events/list">https://dev.twitter.com/rest/reference/get/direct_messages/events/list</a>
   *
   */
  @RemoteMethod(httpMethod = "GET", path = "/show.json")
  public WelcomeMessage showWelcomeMessage(@Query("id") Long id);

  /**
   * Creates a new Welcome Message that will be stored and sent in the future from the authenticating user in defined circumstances. Returns the message template in the requested format if successful. Supports publishing with the same elements as Direct Messages (e.g. Quick Replies, media attachments).
   *
   * @return WelcomeMessage
   * @see <a href="https://dev.twitter.com/rest/reference/get/direct_messages/events/list">https://dev.twitter.com/rest/reference/get/direct_messages/events/list</a>
   *
   */
  @RemoteMethod(httpMethod = "POST", path = "/new.json")
  public WelcomeMessageNewResponse newWelcomeMessage(@Body WelcomeMessageNewRequest messageNewRequest);

  /**
   * Deletes a Welcome Message by the given id.
   *
   * @see <a href="https://dev.twitter.com/rest/reference/del/direct_messages/welcome_messages/destroy">https://dev.twitter.com/rest/reference/del/direct_messages/welcome_messages/destroy</a>
   *
   */
  @RemoteMethod(httpMethod = "DELETE", path = "/destroy.json")
  public void destroyWelcomeMessage(@Query("id") Long id);

}
