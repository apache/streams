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

import org.apache.juneau.http.annotation.Body;
import org.apache.juneau.remote.RemoteInterface;
import org.apache.juneau.remoteable.QueryIfNE;
import org.apache.juneau.rest.client.remote.RemoteMethod;
import org.apache.streams.pipl.api.BasicSearchRequest;
import org.apache.streams.pipl.api.FullPersonSearchRequest;
import org.apache.streams.pipl.api.SearchPointerRequest;
import org.apache.streams.pipl.api.SearchResponse;

@RemoteInterface(path = "https://api.pipl.com/search")
public interface Search {

  @RemoteMethod(method ="GET")
  public SearchResponse basicSearch(@QueryIfNE("*") BasicSearchRequest request);

  @RemoteMethod(method ="POST")
  public SearchResponse fullPersonSearch(@Body FullPersonSearchRequest request);

  @RemoteMethod(method ="POST")
  public SearchResponse pointerSearch(@Body SearchPointerRequest request);

}