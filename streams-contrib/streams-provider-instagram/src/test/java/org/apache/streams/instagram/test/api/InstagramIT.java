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

package org.apache.streams.instagram.test.api;

import org.apache.streams.config.ComponentConfigurator;
import org.apache.streams.config.StreamsConfiguration;
import org.apache.streams.config.StreamsConfigurator;
import org.apache.streams.instagram.api.CommentsResponse;
import org.apache.streams.instagram.api.Instagram;
import org.apache.streams.instagram.api.MediaResponse;
import org.apache.streams.instagram.api.RecentMediaResponse;
import org.apache.streams.instagram.api.Relationship;
import org.apache.streams.instagram.api.Relationships;
import org.apache.streams.instagram.api.SearchUsersRequest;
import org.apache.streams.instagram.api.SearchUsersResponse;
import org.apache.streams.instagram.api.SelfLikedMediaRequest;
import org.apache.streams.instagram.api.SelfRecentMediaRequest;
import org.apache.streams.instagram.api.UserInfoResponse;
import org.apache.streams.instagram.api.Users;
import org.apache.streams.instagram.api.Media;
import org.apache.streams.instagram.api.UsersInfoResponse;
import org.apache.streams.instagram.pojo.UserInfo;
import org.apache.streams.instagram.pojo.UserRecentMediaRequest;
import org.apache.streams.instagram.config.InstagramConfiguration;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

import java.util.List;

import static java.util.Objects.nonNull;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/**
 * Integration Test each Instagram endpoint.
 */
public class InstagramIT {

  private static final Logger LOGGER = LoggerFactory.getLogger(InstagramIT.class);

  private static Config application = ConfigFactory.parseResources("InstagramIT.conf").withFallback(ConfigFactory.load());
  private static StreamsConfiguration streamsConfiguration = StreamsConfigurator.detectConfiguration(application);
  private static InstagramConfiguration config = new ComponentConfigurator<>(InstagramConfiguration.class).detectConfiguration(application, "instagram");

  private static UserInfo user;
  private static List<org.apache.streams.instagram.pojo.Media> recentMedia;
  private static List<org.apache.streams.instagram.pojo.Media> likedMedia;

  @Test(groups = {"SelfUser"})
  public void testSelfUser() throws Exception {
    Users users = Instagram.getInstance(config);
    nonNull(users);
    UserInfoResponse userInfoResponse = users.self();
    nonNull(userInfoResponse);
    nonNull(userInfoResponse.getMeta());
    assertTrue(userInfoResponse.getMeta().getCode() == 200);
    nonNull(userInfoResponse.getData());
    user = userInfoResponse.getData();
  }

  @Test(groups = {"SelfMedia"})
  public void testSelfMediaRecent() throws Exception {
    Users users = Instagram.getInstance(config);
    nonNull(users);
    SelfRecentMediaRequest selfRecentMediaRequest = new SelfRecentMediaRequest();
    RecentMediaResponse selfRecentMediaResponse = users.selfMediaRecent(selfRecentMediaRequest);
    nonNull(selfRecentMediaResponse);
    nonNull(selfRecentMediaResponse.getMeta());
    assertTrue(selfRecentMediaResponse.getMeta().getCode() == 200);
    nonNull(selfRecentMediaResponse.getData());
    assertThat("selfRecentMediaResponse.getData().size() > 0", selfRecentMediaResponse.getData().size() > 0);
    recentMedia = selfRecentMediaResponse.getData();
  }

  @Test(groups = {"SelfMedia"})
  public void testSelfMediaLiked() throws Exception {
    Users users = Instagram.getInstance(config);
    nonNull(users);
    SelfLikedMediaRequest selfLikedMediaRequest = new SelfLikedMediaRequest();
    RecentMediaResponse selfLikedMediaResponse = users.selfMediaLiked(selfLikedMediaRequest);
    nonNull(selfLikedMediaResponse);
    nonNull(selfLikedMediaResponse.getMeta());
    assertTrue(selfLikedMediaResponse.getMeta().getCode() == 200);
    assertThat("selfLikedMediaResponse.getData().size() > 0", selfLikedMediaResponse.getData().size() > 0);
    likedMedia = selfLikedMediaResponse.getData();
  }

  @Test(groups = {"SelfRelationships"})
  public void testFollowedBy() throws Exception {
    Relationships relationships = Instagram.getInstance(config);
    nonNull(relationships);
    SearchUsersResponse followedBy = relationships.followedBy();
    nonNull(followedBy);
    nonNull(followedBy.getMeta());
    assertTrue(followedBy.getMeta().getCode() == 200);
    nonNull(followedBy.getData());
    assertThat("followedBy.getData().size() > 0", followedBy.getData().size() > 0);
  }

  @Test(groups = {"SelfRelationships"})
  public void testFollows() throws Exception {
    Relationships relationships = Instagram.getInstance(config);
    nonNull(relationships);
    SearchUsersResponse follows = relationships.follows();
    nonNull(follows);
    nonNull(follows.getMeta());
    assertTrue(follows.getMeta().getCode() == 200);
    nonNull(follows.getData());
    assertThat("follows.getData().size() > 0", follows.getData().size() > 0);
  }

  @Test(groups = {"SelfRelationships"})
  public void testRequestedBy() throws Exception {
    Relationships relationships = Instagram.getInstance(config);
    nonNull(relationships);
    SearchUsersResponse requestsBy = relationships.requestedBy();
    nonNull(requestsBy);
    nonNull(requestsBy.getMeta());
    assertTrue(requestsBy.getMeta().getCode() == 200);
    nonNull(requestsBy.getData());
  }

  @Test(dependsOnGroups = {"SelfUser"}, groups = {"Users"})
  public void testLookupUser() throws Exception {
    Users users = Instagram.getInstance(config);
    nonNull(users);
    UserInfoResponse userInfoResponse = users.lookupUser(user.getId());
    nonNull(userInfoResponse);
    nonNull(userInfoResponse.getMeta());
    assertTrue(userInfoResponse.getMeta().getCode() == 200);
    nonNull(userInfoResponse.getData());
  }

  @Test(dependsOnGroups = {"SelfUser"}, groups = {"Users"})
  public void testUserMediaRecent() throws Exception {
    Users users = Instagram.getInstance(config);
    nonNull(users);
    UserRecentMediaRequest userRecentMediaRequest = new UserRecentMediaRequest().withUserId(user.getId());
    RecentMediaResponse userRecentMedia = users.userMediaRecent(userRecentMediaRequest);
    nonNull(userRecentMedia);
    nonNull(userRecentMedia.getMeta());
    assertTrue(userRecentMedia.getMeta().getCode() == 200);
    nonNull(userRecentMedia.getData());
    assertThat("userRecentMedia.getData().size() > 0", userRecentMedia.getData().size() > 0);
  }

  @Test(dependsOnGroups = {"SelfUser"}, groups = {"Users"})
  public void testUserSearch() throws Exception {
    Users users = Instagram.getInstance(config);
    nonNull(users);
    SearchUsersRequest searchUsersRequest = new SearchUsersRequest().withQ(user.getBio());
    SearchUsersResponse searchUsersResponse = users.searchUser(searchUsersRequest);
    nonNull(searchUsersResponse);
    nonNull(searchUsersResponse.getMeta());
    assertTrue(searchUsersResponse.getMeta().getCode() == 200);
    nonNull(searchUsersResponse.getData());
    assertThat("searchUsersResponse.getData().size() > 0", searchUsersResponse.getData().size() > 0);
  }

  @Test(dependsOnGroups = {"SelfMedia","SelfUser"}, groups = {"Media"})
  public void testLookupMedia() throws Exception {
    Media media = Instagram.getInstance(config);
    nonNull(media);
    MediaResponse mediaResponse = media.lookupMedia(recentMedia.get(0).getId());
    nonNull(mediaResponse);
    nonNull(mediaResponse.getMeta());
    assertTrue(mediaResponse.getMeta().getCode() == 200);
    nonNull(mediaResponse.getData());
    assertEquals(mediaResponse.getData().getId(), recentMedia.get(0).getId());
  }

  @Test(dependsOnGroups = {"SelfMedia"}, groups = {"Media"})
  public void testComments() throws Exception {
    Media media = Instagram.getInstance(config);
    nonNull(media);
    CommentsResponse comments = media.comments(recentMedia.get(0).getId());
    nonNull(comments);
    nonNull(comments.getMeta());
    assertTrue(comments.getMeta().getCode() == 200);
    nonNull(comments.getData());
    assertThat("comments.getData().size() > 0", comments.getData().size() > 0);
  }

  @Test(dependsOnGroups = {"SelfMedia"}, groups = {"Media"})
  public void testLikes() throws Exception {
    Media media = Instagram.getInstance(config);
    nonNull(media);
    UsersInfoResponse likes = media.likes(recentMedia.get(0).getId());
    nonNull(likes);
    nonNull(likes.getMeta());
    assertTrue(likes.getMeta().getCode() == 200);
    nonNull(likes.getData());
    assertThat("likes.getData().size() > 0", likes.getData().size() > 0);
  }

  @Test(dependsOnGroups = {"SelfMedia","SelfUser"}, groups = {"Media"})
  public void testLookupShortcode() throws Exception {
    Media media = Instagram.getInstance(config);
    nonNull(media);
    MediaResponse mediaResponse = media.shortcode(recentMedia.get(0).getLink());
    nonNull(mediaResponse);
    nonNull(mediaResponse.getMeta());
    assertTrue(mediaResponse.getMeta().getCode() == 200);
    nonNull(mediaResponse.getData());
    assertEquals(mediaResponse.getData().getId(), recentMedia.get(0).getId());
  }

}
