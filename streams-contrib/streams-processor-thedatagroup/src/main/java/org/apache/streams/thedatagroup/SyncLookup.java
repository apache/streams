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

import org.apache.streams.thedatagroup.api.EmailLookupRequest;
import org.apache.streams.thedatagroup.api.IpLookupRequest;
import org.apache.streams.thedatagroup.api.LookupResponse;
import org.apache.streams.thedatagroup.api.PhoneLookupRequest;

import org.apache.juneau.http.annotation.Content;
import org.apache.juneau.http.remote.Remote;
import org.apache.juneau.http.remote.RemotePost;

@Remote(path = "https://api.thedatagroup.com/v3/sync/lookup")
public interface SyncLookup {

  @RemotePost(path="/email")
  public LookupResponse lookupEmail(@Content EmailLookupRequest request);

  @RemotePost(path="/mobile")
  public LookupResponse lookupMobile(@Content PhoneLookupRequest request);

  @RemotePost(path="/ip")
  public LookupResponse lookupIp(@Content IpLookupRequest request);

  @RemotePost(path="/phone")
  public LookupResponse lookupPhone(@Content PhoneLookupRequest request);

}
