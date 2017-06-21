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

package org.apache.streams.instagram.api;

import org.apache.juneau.remoteable.Path;
import org.apache.juneau.remoteable.QueryIfNE;
import org.apache.juneau.remoteable.RemoteMethod;
import org.apache.juneau.remoteable.Remoteable;

/**
 * Media Endpoints.
 * Comments Endpoints.
 * Like Endpoints.
 *
 * @see <a href="https://www.instagram.com/developer/endpoints/media/">https://www.instagram.com/developer/endpoints/media/</a>
 * @see <a href="https://www.instagram.com/developer/endpoints/comments/">https://www.instagram.com/developer/endpoints/comments/</a>
 * @see <a href="https://www.instagram.com/developer/endpoints/likes/">https://www.instagram.com/developer/endpoints/likes/</a>
 */
@Remoteable(path = "/media")
public interface Media {

  /**
   * Get a list of recent comments on a media object.
   * The public_content scope is required for media that does not belong to the owner of the access_token.
   *
   * @see <a href="https://www.instagram.com/developer/endpoints/comments/#get_media_comments">https://www.instagram.com/developer/endpoints/comments/#get_media_comments</a>
   * @param media_id media_id
   * @return CommentsResponse @link{CommentsResponse}
   */
  @RemoteMethod(httpMethod = "GET", path = "/{media_id}/comments")
  public CommentsResponse comments( @Path("media_id") String media_id);

  /**
   * Get a list of users who have liked this media.
   * The public_content scope is required for media that does not belong to the owner of the access_token.
   *
   * @see <a href="https://www.instagram.com/developer/endpoints/likes/#get_media_likes">https://www.instagram.com/developer/endpoints/likes/#get_media_likes</a>
   * @param media_id media_id
   * @return UsersInfoResponse @link{UsersInfoResponse}
   */
  @RemoteMethod(httpMethod = "GET", path = "/{media_id}/likes")
  public UsersInfoResponse likes( @Path("media_id") String media_id);

  /**
   * Get information about a media object. Use the type field to differentiate between image and video media in the response. You will also receive the user_has_liked field which tells you whether the owner of the access_token has liked this media.
   *
   * The public_content permission scope is required to get a media that does not belong to the owner of the access_token.
   *
   * @see <a href="https://www.instagram.com/developer/endpoints/media/#get_media">https://www.instagram.com/developer/endpoints/media/#get_media</a>
   * @param media_id media_id
   * @return MediaResponse @link{MediaResponse}
   */
  @RemoteMethod(httpMethod = "GET", path = "/{media_id}")
  public MediaResponse lookupMedia( @Path("media_id") String media_id);

  /**
   * This endpoint returns the same response as GET /media/media-id.
   * A media object's shortcode can be found in its shortlink URL. An example shortlink is http://instagram.com/p/tsxp1hhQTG/. Its corresponding shortcode is tsxp1hhQTG.
   *
   * @see <a href="https://www.instagram.com/developer/endpoints/media/#get_media_by_shortcode">https://www.instagram.com/developer/endpoints/media/#get_media_by_shortcode</a>
   * @param shortcode shortcode
   * @return MediaResponse @link{MediaResponse}
   */
  @RemoteMethod(httpMethod = "GET", path = "/shortcode/{shortcode}")
  public MediaResponse shortcode( @Path("shortcode") String shortcode);

  /**
   * Search for recent media in a given area.
   *
   * @see <a href="https://www.instagram.com/developer/endpoints/media/#get_media_search">https://www.instagram.com/developer/endpoints/media/#get_media_search</a>
   * @param parameters @link{SearchMediaRequest}
   * @return SearchMediaResponse @link{SearchMediaResponse}
   */
  @RemoteMethod(httpMethod = "GET", path = "/search")
  public SearchMediaResponse searchMedia( @QueryIfNE("*") SearchMediaRequest parameters);
}
