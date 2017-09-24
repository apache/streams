package org.apache.streams.twitter.api;

import org.apache.streams.twitter.pojo.WelcomeMessage;
import org.apache.streams.twitter.pojo.WelcomeMessageRule;

import org.apache.juneau.remoteable.Body;
import org.apache.juneau.remoteable.Query;
import org.apache.juneau.remoteable.QueryIfNE;
import org.apache.juneau.remoteable.RemoteMethod;
import org.apache.juneau.remoteable.Remoteable;

/**
 * Interface for /direct_messages/welcome_messages/rules methods.
 *
 * @see <a href="https://dev.twitter.com/rest/reference">https://dev.twitter.com/rest/reference</a>
 */
@Remoteable(path = "https://api.twitter.com/1.1/direct_messages/welcome_messages/rules")
public interface WelcomeMessageRules {

  /**
   * Returns a list of Welcome Message Rules.
   *
   * @return WelcomeMessageRulesListResponse
   * @see <a href="https://dev.twitter.com/rest/reference/get/direct_messages/welcome_messages/rules/list">https://dev.twitter.com/rest/reference/get/direct_messages/welcome_messages/rules/list</a>
   *
   */
  @RemoteMethod(httpMethod = "GET", path = "/list.json")
  public WelcomeMessageRulesListResponse listWelcomeMessageRules(@QueryIfNE WelcomeMessageRulesListRequest parameters);

  /**
   * Returns a Welcome Message Rule by the given id.
   *
   * @return WelcomeMessage
   * @see <a href="https://dev.twitter.com/rest/reference/get/direct_messages/events/list">https://dev.twitter.com/rest/reference/get/direct_messages/events/list</a>
   *
   */
  @RemoteMethod(httpMethod = "GET", path = "/show.json")
  public WelcomeMessageRule showWelcomeMessageRule(@Query("id") Long id);

  /**
   * Creates a new Welcome Message Rule that determines which Welcome Message will be shown in a given conversation. Returns the created rule if successful.
   *
   * Requires a JSON POST body and Content-Type header to be set to application/json. Setting Content-Length may also be required if it is not automatically.
   *
   * Additional rule configurations are forthcoming. For the initial beta release, the most recently created Rule will always take precedence, and the assigned Welcome Message will be displayed in the conversation.
   *
   * @return WelcomeMessage
   * @see <a href="https://dev.twitter.com/rest/reference/post/direct_messages/welcome_messages/rules/new">https://dev.twitter.com/rest/reference/post/direct_messages/welcome_messages/rules/new</a>
   *
   */
  @RemoteMethod(httpMethod = "POST", path = "/new.json")
  public WelcomeMessageRule newWelcomeMessageRule(@Body WelcomeMessageNewRuleRequest body);

  /**
   * Deletes a Welcome Message Rule by the given id.
   *
   * @see <a href="https://dev.twitter.com/rest/reference/del/direct_messages/welcome_messages/rules/destroy">https://dev.twitter.com/rest/reference/del/direct_messages/welcome_messages/rules/destroy</a>
   *
   */
  @RemoteMethod(httpMethod = "DELETE", path = "/destroy.json")
  public void destroyWelcomeMessageRule(@Query("id") Long id);

}
