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

package org.apache.streams.facebook.test;

import org.apache.streams.facebook.Page;
import org.apache.streams.facebook.Post;
import org.apache.streams.facebook.provider.FacebookEventClassifier;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class FacebookEventClassifierTest {

  private String empty = "";
  private String def = "{}";
  private String post = "{\"metadata\":null,\"id\":\"687664828_10153082499914829\",\"from\":{\"metadata\":null,\"id\":\"687664828\",\"name\":\"Steve Blackmon\",\"category\":null,\"createdTime\":null},\"message\":null,\"picture\":\"https://fbexternal-a.akamaihd.net/app_full_proxy.php?app=184136951108&v=1&size=z&cksum=7f4094dff37cedd69072cd2c0b3728b7&src=https%3A%2F%2Fstatic.tripit.com%2Fimages%2Fplaces%2Fsantamonica.jpg%3Fv%3D2014-08-13\",\"link\":\"http://www.tripit.com/trip/show/id/130372979/traveler_fb_uid/687664828?us=fc&um=fa&un=fd\",\"name\":\"Steve is about to leave on a trip to Santa Monica, CA.\",\"caption\":\"Aug 2014 for 3 days\",\"description\":\"TripIt - Free online trip planner and free travel itinerary website for organizing vacations, group trips or business travel\",\"source\":null,\"icon\":\"https://fbcdn-photos-d-a.akamaihd.net/hphotos-ak-xpa1/t39.2080-0/851580_10151367869221109_1073679965_n.gif\",\"actions\":[{\"name\":\"Comment\",\"link\":\"https://www.facebook.com/687664828/posts/10153082499914829\"},{\"name\":\"Like\",\"link\":\"https://www.facebook.com/687664828/posts/10153082499914829\"},{\"name\":\"Plan a trip on TripIt\",\"link\":\"http://www.tripit.com/?us=fc&um=fa&un=fd\"}],\"privacy\":{\"value\":\"ALL_FRIENDS\",\"friends\":\"EMPTY\",\"description\":[\"Your friends\"]},\"type\":\"link\",\"sharesCount\":null,\"place\":null,\"statusType\":\"app_created_story\",\"story\":null,\"objectId\":null,\"application\":{\"id\":\"184136951108\",\"name\":\"TripIt\",\"description\":null,\"category\":null,\"company\":null,\"iconUrl\":null,\"subcategory\":null,\"link\":null,\"logoUrl\":null,\"dailyActiveUsers\":null,\"weeklyActiveUsers\":null,\"monthlyActiveUsers\":null,\"namespace\":\"tripitcom\",\"authDialogDataHelpUrl\":null,\"authDialogDescription\":null,\"authDialogHeadline\":null,\"authDialogPermsExplanation\":null,\"authReferralDefaultActivityPrivacy\":null,\"authReferralResponseType\":null,\"canvasUrl\":null,\"contactEmail\":null,\"createdTime\":null,\"creatorUid\":null,\"deauthCallbackUrl\":null,\"iphoneAppStoreId\":null,\"hostingUrl\":null,\"mobileWebUrl\":null,\"pageTabDefaultName\":null,\"pageTabUrl\":null,\"privacyPolicyUrl\":null,\"secureCanvasUrl\":null,\"securePageTabUrl\":null,\"serverIpWhitelist\":null,\"termsOfServiceUrl\":null,\"userSupportEmail\":null,\"userSupportUrl\":null,\"websiteUrl\":null,\"canvasName\":null},\"createdTime\":\"2014-08-13T12:22:20.000+0000\",\"updatedTime\":\"2014-08-13T12:22:20.000+0000\",\"scheduledPublishTime\":null,\"targeting\":null,\"published\":null}";
  private String page = "{\"metadata\":null,\"id\":\"142803045874943\",\"name\":\"Senator Angus S. King, Jr.\",\"category\":\"Government official\",\"createdTime\":null,\"link\":\"https://www.facebook.com/SenatorAngusSKingJr\",\"likes\":10246,\"location\":{\"street\":\"359 Dirksen Senate Office Building\",\"city\":\"Washington, District of Columbia\",\"state\":\"DC\",\"country\":\"United States\",\"zip\":\"20510\",\"latitude\":null,\"longitude\":null,\"text\":null},\"phone\":\"202-224-5344\",\"checkins\":0,\"picture\":null,\"cover\":{\"id\":null,\"source\":\"https://fbcdn-sphotos-g-a.akamaihd.net/hphotos-ak-xpa1/v/t1.0-9/10288792_321537751334804_8200105519500362465_n.jpg?oh=fbcde9b3e1e011dfa3e699628629bc53&oe=546FB617&__gda__=1416717487_3fa5781d7d9c3d58f2bc798a36ac6fc0\",\"offsetY\":9},\"website\":\"http://www.king.senate.gov\",\"talkingAboutCount\":5034,\"accessToken\":null,\"wereHereCount\":0,\"about\":\"Welcome to the official Facebook page of Senator Angus S. King, Jr. (I-ME).\\nhttp://king.senate.gov\\nhttps://twitter.com/SenAngusKing\\nhttps://www.youtube.com/SenatorAngusKing\",\"username\":\"SenatorAngusSKingJr\",\"published\":true,\"communityPage\":false}";

  @Test(expected=IllegalArgumentException.class)
  public void emptyJSONTest() {
    Class inClass = FacebookEventClassifier.detectClass(empty);
  }

  @Test
  public void defaultDetectTest() {
    Class inClass = FacebookEventClassifier.detectClass(post);
    assertEquals(inClass, Post.class);
  }

  @Test
  public void postDetectTest() {
    Class inClass = FacebookEventClassifier.detectClass(post);
    assertEquals(inClass, Post.class);
  }

  @Test
  public void pageDetectTest() {
    Class inClass = FacebookEventClassifier.detectClass(page);
    assertEquals(inClass, Page.class);
  }
}