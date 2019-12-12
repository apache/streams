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

package org.apache.streams.peopledatalabs;

import org.apache.juneau.rest.client.RestCallException;
import org.apache.streams.peopledatalabs.api.BulkEnrichPersonResponseItem;
import org.apache.streams.peopledatalabs.api.EnrichPersonRequest;
import org.apache.streams.peopledatalabs.api.BulkEnrichPersonRequest;
import org.apache.streams.peopledatalabs.api.EnrichPersonResponse;

import org.apache.juneau.http.annotation.Body;
import org.apache.juneau.http.annotation.Query;
import org.apache.juneau.remote.RemoteInterface;
import org.apache.juneau.rest.client.remote.RemoteMethod;

import java.util.List;

@RemoteInterface(path = "https://api.peopledatalabs.com/v4")
public interface PersonEnrichment {

    @RemoteMethod(method ="GET", path="/person")
    public EnrichPersonResponse enrichPerson(@Query(name = "*", skipIfEmpty = true) EnrichPersonRequest request);

    @RemoteMethod(method ="POST", path="/person/bulk")
    public List<BulkEnrichPersonResponseItem> bulkEnrichPerson(@Body(required = true) BulkEnrichPersonRequest request);

}