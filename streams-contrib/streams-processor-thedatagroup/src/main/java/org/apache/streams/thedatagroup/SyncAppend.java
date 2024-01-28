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

package org.apache.streams.thedatagroup;

import org.apache.streams.thedatagroup.api.AppendRequest;
import org.apache.streams.thedatagroup.api.DemographicsAppendResponse;
import org.apache.streams.thedatagroup.api.EmailAppendResponse;
import org.apache.streams.thedatagroup.api.MobileAppendResponse;
import org.apache.streams.thedatagroup.api.PhoneAppendResponse;
import org.apache.streams.thedatagroup.api.VehicleAppendResponse;

import org.apache.juneau.http.annotation.Content;
import org.apache.juneau.http.remote.Remote;
import org.apache.juneau.http.remote.RemotePost;

@Remote(path = "https://api.thedatagroup.com/v3/sync/append")
public interface SyncAppend {

  @RemotePost(path="/demographics")
  public DemographicsAppendResponse appendDemographics(@Content AppendRequest request);

  @RemotePost(path="/email")
  public EmailAppendResponse appendEmail(@Content AppendRequest request);

  @RemotePost(path="/mobile")
  public MobileAppendResponse appendMobile(@Content AppendRequest request);

  @RemotePost(path="/phone")
  public PhoneAppendResponse appendPhone(@Content AppendRequest request);

  @RemotePost(path="/vehicle")
  public VehicleAppendResponse appendVehicle(@Content AppendRequest request);

}
