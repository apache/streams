package org.apache.streams.instagram.api;

/**
 * Created by sblackmon on 4/11/17.
 */
public interface Media {

  public CommentsResponse comments(String media_id);

  public UsersInfoResponse likes(String media_id);

  public MediaResponse lookupMedia(String media_id);

  public MediaResponse shortcode(String shortcode);

  public SearchMediaResponse searchMedia(SearchMediaRequest parameters);
}
