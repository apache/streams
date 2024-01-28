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

package org.apache.streams.pipl;

import org.apache.streams.pipl.api.BasicSearchRequest;
import org.apache.streams.pipl.api.FullPersonSearchRequest;
import org.apache.streams.pipl.api.SearchPointerRequest;
import org.apache.streams.pipl.api.SearchResponse;

import org.apache.juneau.http.annotation.Content;
import org.apache.juneau.http.annotation.Query;
import org.apache.juneau.http.remote.Remote;
import org.apache.juneau.http.remote.RemoteGet;
import org.apache.juneau.http.remote.RemotePost;

@Remote(path = "https://api.pipl.com/search")
public interface Search {

  @RemoteGet
  public SearchResponse basicSearch(@Query("*") BasicSearchRequest request);

  @RemotePost
  public SearchResponse fullPersonSearch(@Content FullPersonSearchRequest request);

  @RemotePost
  public SearchResponse pointerSearch(@Content SearchPointerRequest request);

}
