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

import org.apache.juneau.http.annotation.Body;
import org.apache.juneau.remote.RemoteInterface;
import org.apache.juneau.rest.client.remote.RemoteMethod;
import org.apache.streams.thedatagroup.api.*;

@RemoteInterface(path = "https://api.thedatagroup.com/v3/sync/append")
public interface SyncAppend {

  @RemoteMethod(method ="POST", path="/demographics")
  public DemographicsAppendResponse appendDemographics(@Body AppendRequest request);

  @RemoteMethod(method ="POST", path="/email")
  public EmailAppendResponse appendEmail(@Body AppendRequest request);

  @RemoteMethod(method ="POST", path="/mobile")
  public MobileAppendResponse appendMobile(@Body AppendRequest request);

  @RemoteMethod(method ="POST", path="/phone")
  public PhoneAppendResponse appendPhone(@Body AppendRequest request);

  @RemoteMethod(method ="POST", path="/vehicle")
  public VehicleAppendResponse appendVehicle(@Body AppendRequest request);

}