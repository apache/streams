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

package org.apache.streams.sprinklr;

import org.apache.streams.sprinklr.api.ProfileConversationsRequest;
import org.apache.streams.sprinklr.api.ProfileConversationsResponse;
import org.apache.streams.sprinklr.api.SocialProfileRequest;
import org.apache.streams.sprinklr.api.SocialProfileResponse;

import org.apache.juneau.http.remote.Remote;
import org.apache.juneau.http.remote.RemoteGet;

import java.util.List;

@Remote(path = "https://api2.sprinklr.com/api/")
public interface Profiles {

  @RemoteGet
  public List<SocialProfileResponse> getSocialProfile(SocialProfileRequest request);

  @RemoteGet
  public List<ProfileConversationsResponse> getProfileConversations(ProfileConversationsRequest request);

  @RemoteGet
  public List<ProfileConversationsResponse> getAllProfileConversations(ProfileConversationsRequest request);
}
