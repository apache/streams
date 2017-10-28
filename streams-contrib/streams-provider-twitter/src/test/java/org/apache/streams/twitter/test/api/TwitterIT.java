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

package org.apache.streams.twitter.test.api;

import org.apache.streams.config.ComponentConfigurator;
import org.apache.streams.config.StreamsConfiguration;
import org.apache.streams.config.StreamsConfigurator;
import org.apache.streams.twitter.TwitterConfiguration;
import org.apache.streams.twitter.api.Account;
import org.apache.streams.twitter.api.AccountActivity;
import org.apache.streams.twitter.api.AccountSettings;
import org.apache.streams.twitter.api.DirectMessages;
import org.apache.streams.twitter.api.EventsListRequest;
import org.apache.streams.twitter.api.EventsListResponse;
import org.apache.streams.twitter.api.Favorites;
import org.apache.streams.twitter.api.FavoritesListRequest;
import org.apache.streams.twitter.api.Followers;
import org.apache.streams.twitter.api.FollowersIdsRequest;
import org.apache.streams.twitter.api.FollowersIdsResponse;
import org.apache.streams.twitter.api.FollowersListRequest;
import org.apache.streams.twitter.api.FollowersListResponse;
import org.apache.streams.twitter.api.Friends;
import org.apache.streams.twitter.api.FriendsIdsRequest;
import org.apache.streams.twitter.api.FriendsIdsResponse;
import org.apache.streams.twitter.api.FriendsListRequest;
import org.apache.streams.twitter.api.FriendsListResponse;
import org.apache.streams.twitter.api.SevenDaySearch;
import org.apache.streams.twitter.api.SevenDaySearchRequest;
import org.apache.streams.twitter.api.SevenDaySearchResponse;
import org.apache.streams.twitter.api.Statuses;
import org.apache.streams.twitter.api.StatusesHomeTimelineRequest;
import org.apache.streams.twitter.api.StatusesLookupRequest;
import org.apache.streams.twitter.api.StatusesMentionsTimelineRequest;
import org.apache.streams.twitter.api.StatusesShowRequest;
import org.apache.streams.twitter.api.SuggestedUserCategory;
import org.apache.streams.twitter.api.SuggestedUsers;
import org.apache.streams.twitter.api.Twitter;
import org.apache.streams.twitter.api.Users;
import org.apache.streams.twitter.api.UsersLookupRequest;
import org.apache.streams.twitter.api.UsersSearchRequest;
import org.apache.streams.twitter.api.UsersShowRequest;
import org.apache.streams.twitter.api.Webhook;
import org.apache.streams.twitter.api.WelcomeMessageRules;
import org.apache.streams.twitter.api.WelcomeMessageRulesListRequest;
import org.apache.streams.twitter.api.WelcomeMessageRulesListResponse;
import org.apache.streams.twitter.api.WelcomeMessages;
import org.apache.streams.twitter.api.WelcomeMessagesListRequest;
import org.apache.streams.twitter.api.WelcomeMessagesListResponse;
import org.apache.streams.twitter.pojo.Tweet;
import org.apache.streams.twitter.pojo.User;
import org.apache.streams.twitter.pojo.WelcomeMessage;
import org.apache.streams.twitter.pojo.WelcomeMessageRule;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.apache.juneau.remoteable.Path;
import org.apache.juneau.remoteable.QueryIfNE;
import org.apache.juneau.remoteable.RemoteMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.File;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.Objects.nonNull;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/**
 * Integration Tests for all implemented Twitter endpoints.
 */
public class TwitterIT {

  private static final Logger LOGGER = LoggerFactory.getLogger(TwitterIT.class);

  private static String configfile = "target/test-classes/TwitterIT.conf";

  private static Config application;
  private static TwitterConfiguration config;

  private static User user;
  private static AccountSettings settings;
  private static List<Tweet> statusesHomeTimeline;

  @BeforeClass
  public void setup() throws Exception {
    File conf = new File(configfile);
    Assert.assertTrue (conf.exists());
    Assert.assertTrue (conf.canRead());
    Assert.assertTrue (conf.isFile());
    application = ConfigFactory.parseFileAnySyntax(conf).withFallback(ConfigFactory.load());
    config = new ComponentConfigurator<>(TwitterConfiguration.class).detectConfiguration(application, "twitter");
  }

  @Test(groups = {"Account","AccountVerifyCredentials"})
  public void testVerifyCredentials() throws Exception {
    Account account = Twitter.getInstance(config);
    nonNull(account);
    User user = account.verifyCredentials();
    nonNull(user);
    nonNull(user.getCreatedAt());
    TwitterIT.user = user;
  }

  @Test(dependsOnGroups = {"AccountVerifyCredentials"}, groups = {"Account"})
  public void testAccountSettings() throws Exception {
    Account account = Twitter.getInstance(config);
    nonNull(account);
    AccountSettings settings = account.settings();
    nonNull(settings);
    nonNull(settings.getScreenName());
    assertEquals(settings.getScreenName(), user.getScreenName());
    TwitterIT.settings = settings;
  }

  @Test(dependsOnGroups = {"Account"}, groups = {"Favorites"})
  public void testFavoritesList() throws Exception {
    Favorites favorites = Twitter.getInstance(config);
    nonNull(favorites);
    List<Tweet> favoritesTweetList = favorites.list(new FavoritesListRequest().withScreenName(user.getScreenName()));
    nonNull(favoritesTweetList);
    assertThat("favoritesTweetList.size() > 0", favoritesTweetList.size() > 0);
  }

  @Test(dependsOnGroups = {"Account"}, groups = {"Followers"})
  public void testFollowersList() throws Exception {
    Followers followers = Twitter.getInstance(config);
    nonNull(followers);
    FollowersListRequest followersListRequest = new FollowersListRequest();
    followersListRequest.setId(user.getId());
    FollowersListResponse followersListResponse = followers.list(followersListRequest);
    nonNull(followersListResponse);
    assertThat("followersListResponse.getUsers().size() > 0", followersListResponse.getUsers().size() > 0);
  }

  @Test(dependsOnGroups = {"Account"}, groups = {"Followers"})
  public void testFollowersIds() throws Exception {
    Followers followers = Twitter.getInstance(config);
    nonNull(followers);
    FollowersIdsRequest followersIdsRequest = new FollowersIdsRequest();
    followersIdsRequest.setId(user.getId());
    FollowersIdsResponse followersIdsResponse = followers.ids(followersIdsRequest);
    nonNull(followersIdsResponse);
    assertThat("followersIdsResponse.getUsers().size() > 0", followersIdsResponse.getIds().size() > 0);
  }

  @Test(dependsOnGroups = {"Account"}, groups = {"Friends"})
  public void testFriendsList() throws Exception {
    Friends friends = Twitter.getInstance(config);
    nonNull(friends);
    FriendsListRequest friendsListRequest = (FriendsListRequest) (new FriendsListRequest().withId(user.getId()));
    FriendsListResponse friendsListResponse = friends.list(friendsListRequest);
    nonNull(friendsListResponse);
    assertThat("friendsListResponse.getUsers().size() > 0", friendsListResponse.getUsers().size() > 0);
  }

  @Test(dependsOnGroups = {"Account"}, groups = {"Friends"})
  public void testFriendsIds() throws Exception {
    Friends friends = Twitter.getInstance(config);
    nonNull(friends);
    FriendsIdsRequest friendsIdsRequest = (FriendsIdsRequest)new FriendsIdsRequest().withId(user.getId());
    FriendsIdsResponse friendsIdsResponse = friends.ids(friendsIdsRequest);
    nonNull(friendsIdsResponse);
    assertThat("friendsIdsResponse.getUsers().size() > 0", friendsIdsResponse.getIds().size() > 0);
  }

  @Test(dependsOnGroups = {"Account"}, groups = {"Statuses","StatusesHomeTimeline"})
  public void testStatusesHomeTimeline() throws Exception {
    Statuses statuses = Twitter.getInstance(config);
    nonNull(statuses);
    StatusesHomeTimelineRequest statusesHomeTimelineRequest = new StatusesHomeTimelineRequest();
    List<Tweet> statusesHomeTimeline = statuses.homeTimeline(statusesHomeTimelineRequest);
    nonNull(statusesHomeTimeline);
    assertThat("statusesHomeTimeline.size() > 0", statusesHomeTimeline.size() > 0);
    this.statusesHomeTimeline = statusesHomeTimeline;
  }

  @Test(dependsOnGroups = {"Account"}, groups = {"Statuses"})
  public void testStatusesMentionsTimeline() throws Exception {
    Statuses statuses = Twitter.getInstance(config);
    nonNull(statuses);
    StatusesMentionsTimelineRequest statusesMentionsTimelineRequest = new StatusesMentionsTimelineRequest();
    List<Tweet> statusesMentionsTimeline = statuses.mentionsTimeline(statusesMentionsTimelineRequest);
    nonNull(statusesMentionsTimeline);
    assertThat("statusesMentionsTimeline.size() > 0", statusesMentionsTimeline.size() > 0);
  }

  @Test(dependsOnGroups = {"Account","StatusesHomeTimeline"}, groups = {"Statuses"})
  public void testStatusesLookup() throws Exception {
    Statuses statuses = Twitter.getInstance(config);
    nonNull(statuses);
    StatusesLookupRequest statusesLookupRequest = new StatusesLookupRequest();
    statusesLookupRequest.setId(statusesHomeTimeline.stream().map(tweet -> tweet.getId()).collect(Collectors.toList()));
    List<Tweet> statusesLookup = statuses.lookup(statusesLookupRequest);
    nonNull(statusesLookup);
    assertThat("statusesLookup.size() > 0", statusesLookup.size() > 0);
  }

  @Test(dependsOnGroups = {"Account"}, groups = {"Statuses"})
  public void testStatusesShow() throws Exception {
    Statuses statuses = Twitter.getInstance(config);
    nonNull(statuses);
    StatusesShowRequest statusesShowRequest = new StatusesShowRequest();
    statusesShowRequest.setId(user.getStatus().getId());
    Tweet statusesShow = statuses.show(statusesShowRequest);
    nonNull(statusesShow);
    nonNull(statusesShow.getCreatedAt());
  }

  @Test(dependsOnGroups = {"Account"}, groups = {"Users"})
  public void testUsersShow() throws Exception {
    Users users = Twitter.getInstance(config);
    nonNull(users);
    User showUser = users.show(new UsersShowRequest().withScreenName(settings.getScreenName()));
    nonNull(showUser);
    assertEquals( settings.getScreenName(), showUser.getScreenName());
  }

  @Test(dependsOnGroups = {"Account"}, groups = {"Users"})
  public void testUsersLookupById() throws Exception {
    Users users = Twitter.getInstance(config);
    nonNull(users);
    UsersLookupRequest usersLookupRequest = new UsersLookupRequest();
    usersLookupRequest.setUserId(statusesHomeTimeline.stream().map(tweet -> tweet.getUser().getId()).collect(Collectors.toList()));
    List<User> lookupUserById = users.lookup(usersLookupRequest);
    nonNull(lookupUserById);
    assertThat("lookupUserById.size() > 0", lookupUserById.size() > 0);
  }

  @Test(dependsOnGroups = {"Account"}, groups = {"Users"})
  public void testUsersLookupByScreenName() throws Exception {
    Users users = Twitter.getInstance(config);
    nonNull(users);
    UsersLookupRequest usersLookupRequest = new UsersLookupRequest();
    usersLookupRequest.setScreenName(statusesHomeTimeline.stream().map(tweet -> tweet.getUser().getScreenName()).collect(Collectors.toList()));
    List<User> lookupUserByScreenName = users.lookup(usersLookupRequest);
    nonNull(lookupUserByScreenName);
    assertThat("lookupUserByScreenName.size() > 0", lookupUserByScreenName.size() > 0);
  }

  @Test(dependsOnGroups = {"Account"}, groups = {"Users"})
  public void testUserSearch() throws Exception {
    Users users = Twitter.getInstance(config);
    nonNull(users);
    UsersSearchRequest searchRequest = new UsersSearchRequest();
    searchRequest.setQ("big data");
    List<User> searchResponse = users.search(searchRequest);
    nonNull(searchResponse);
    assertThat("searchResponse.size() > 0", searchResponse.size() > 0);
  }

  @Test(dependsOnGroups = {"Account"}, groups = {"Search"})
  public void testSevenDaySearch() throws Exception {
    SevenDaySearch search = Twitter.getInstance(config);
    nonNull(search);
    SevenDaySearchRequest searchRequest = new SevenDaySearchRequest();
    searchRequest.setQ("big data");
    SevenDaySearchResponse searchResponse = search.sevenDaySearch(searchRequest);
    nonNull(searchResponse);
    assertThat("searchResponse.getStatuses().size() > 0", searchResponse.getStatuses().size() > 0);
  }

  @Test(dependsOnGroups = {"Account"}, groups = {"SuggestedUsers"})
  public void testSuggestedUsersCategories() throws Exception {
    String testLang = "en";
    SuggestedUsers suggestedUsers = Twitter.getInstance(config);
    nonNull(suggestedUsers);
    List<SuggestedUserCategory> categories = suggestedUsers.categories(testLang);
    nonNull(categories);
    assertThat("categories.size() > 0", categories.size() > 0);
    String firstSlug = categories.get(0).getSlug();
    SuggestedUserCategory firstCategory = suggestedUsers.suggestions(firstSlug, testLang);
    nonNull(firstCategory);
    assertThat("firstCategory.getUsers().size() > 0", firstCategory.getUsers().size() > 0);
    List<User> members = suggestedUsers.members(firstSlug);
    nonNull(members);
    assertThat("members.size() > 0", members.size() > 0);
  }

  @Test(
      enabled=false,
      dependsOnGroups = {"Account"},
      groups = {"DirectMessages"}
  )
  public void testDirectMessagesListEvents() throws Exception {
    DirectMessages directMessages = Twitter.getInstance(config);
    nonNull(directMessages);
    EventsListResponse eventsListResponse = directMessages.listEvents(new EventsListRequest());
    nonNull(eventsListResponse);
    List<Object> events = eventsListResponse.getEvents();
    nonNull(events);
    assertThat("events.size() > 0", events.size() > 0);
  }

  @Test(
      enabled=false,
      dependsOnGroups = {"Account"},
      groups = {"AccountActivity"}
  )
  public void testGetWebhooks() throws Exception {
    AccountActivity accountActivity = Twitter.getInstance(config);
    nonNull(accountActivity);
    List<Webhook> webhooks = accountActivity.getWebhooks();
    nonNull(webhooks);
  }

  @Test(
      enabled=false,
      dependsOnGroups = {"Account"},
      groups = {"WelcomeMessages"}
  )
  public void testGetWelcomeMessages() throws Exception {
    WelcomeMessages welcomeMessages = Twitter.getInstance(config);
    nonNull(welcomeMessages);
    WelcomeMessagesListResponse welcomeMessageListResponse = welcomeMessages.listWelcomeMessages(new WelcomeMessagesListRequest());
    List<WelcomeMessage> welcomeMessageList = welcomeMessageListResponse.getWelcomeMessages();
    nonNull(welcomeMessageList);
  }

  @Test(
      enabled=false,
      dependsOnGroups = {"Account"},
      groups = {"WelcomeMessageRules"}
  )
  public void testGetWelcomeMessageRules() throws Exception {
    WelcomeMessageRules welcomeMessageRules = Twitter.getInstance(config);
    nonNull(welcomeMessageRules);
    WelcomeMessageRulesListResponse welcomeMessageListResponse = welcomeMessageRules.listWelcomeMessageRules(new WelcomeMessageRulesListRequest());
    List<WelcomeMessageRule> welcomeMessageRuleList = welcomeMessageListResponse.getWelcomeMessageRules();
    nonNull(welcomeMessageRuleList);
  }
}