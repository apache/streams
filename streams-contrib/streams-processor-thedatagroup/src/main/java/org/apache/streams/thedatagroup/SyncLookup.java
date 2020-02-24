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
import org.apache.streams.thedatagroup.api.EmailLookupRequest;
import org.apache.streams.thedatagroup.api.IpLookupRequest;
import org.apache.streams.thedatagroup.api.LookupResponse;
import org.apache.streams.thedatagroup.api.PhoneLookupRequest;

@RemoteInterface(path = "https://api.thedatagroup.com/v3/sync/lookup")
public interface SyncLookup {

  @RemoteMethod(method ="POST", path="/email")
  public LookupResponse lookupEmail(@Body EmailLookupRequest request);

  @RemoteMethod(method ="POST", path="/mobile")
  public LookupResponse lookupMobile(@Body PhoneLookupRequest request);

  @RemoteMethod(method ="POST", path="/ip")
  public LookupResponse lookupIp(@Body IpLookupRequest request);

  @RemoteMethod(method ="POST", path="/phone")
  public LookupResponse lookupPhone(@Body PhoneLookupRequest request);

}