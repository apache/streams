package org.apache.streams.twitter.api;

import org.apache.streams.twitter.pojo.Tweet;

import org.apache.juneau.remoteable.QueryIfNE;
import org.apache.juneau.remoteable.RemoteMethod;
import org.apache.juneau.remoteable.Remoteable;

import java.util.List;

/**
 * Interface for /media methods.
 *
 * @see <a href="https://dev.twitter.com/rest/reference">https://dev.twitter.com/rest/reference</a>
 */
@Remoteable(path = "https://upload.twitter.com/1.1/media")
public interface Media {

  /**
   * The STATUS command is used to periodically poll for updates of media processing operation. After the STATUS command response returns succeeded, you can move on to the next step which is usually create Tweet with media_id.
   *
   * @param parameters {@link org.apache.streams.twitter.api.MediaStatusRequest}
   * @return {@link org.apache.streams.twitter.api.MediaStatusResponse)
   * @see <a href="https://developer.twitter.com/en/docs/media/upload-media/api-reference/get-media-upload-status">https://developer.twitter.com/en/docs/media/upload-media/api-reference/get-media-upload-status</a>
   *
   */
  @RemoteMethod(httpMethod = "GET", path = "/upload")
  public MediaStatusResponse getMediaStatus(@QueryIfNE MediaStatusRequest parameters );

}
