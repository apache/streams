package org.apache.streams.instagram.api;

import org.apache.streams.instagram.pojo.UserInfo;
import org.apache.streams.instagram.pojo.UserRecentMediaRequest;

/**
 * Created by sblackmon on 4/11/17.
 */
public interface Users {

  public UserInfoResponse self();

  public UserInfoResponse lookupUser(String user_id);

  public RecentMediaResponse selfMediaRecent(SelfRecentMediaRequest parameters);

  public RecentMediaResponse userMediaRecent(UserRecentMediaRequest parameters);

  public RecentMediaResponse selfMediaLiked(SelfLikedMediaRequest parameters);

  public SearchUsersResponse searchUser(SearchUsersRequest parameters);
}
