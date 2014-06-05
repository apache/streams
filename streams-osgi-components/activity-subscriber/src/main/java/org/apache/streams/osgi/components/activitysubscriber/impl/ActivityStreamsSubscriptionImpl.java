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

package org.apache.streams.osgi.components.activitysubscriber.impl;


import org.apache.streams.osgi.components.activitysubscriber.ActivityStreamsSubscription;
import org.apache.streams.osgi.components.activitysubscriber.ActivityStreamsSubscriptionOutput;

import org.codehaus.jackson.map.annotate.JsonDeserialize;

import java.util.ArrayList;
import java.util.List;

public class ActivityStreamsSubscriptionImpl implements ActivityStreamsSubscription {

    @JsonDeserialize(as=ArrayList.class)
    private List<String> filters;

    @JsonDeserialize(as=ArrayList.class)
    private List<ActivityStreamsSubscriptionOutput> outputs;

    private String authToken;

    public void setFilters(List<String> filters) {
        this.filters = filters;
    }

    @Override
    public List<ActivityStreamsSubscriptionOutput> getActivityStreamsSubscriptionOutputs() {
        return outputs;
    }

    @Override
    public void setActivityStreamsSubscriptionOutputs(List<ActivityStreamsSubscriptionOutput> outputs) {
        this.outputs = outputs;
    }

    @Override
    public List<String> getFilters(){
        return filters;

    }

    @Override
    public String getAuthToken() {
        return authToken;
    }

    @Override
    public void setAuthToken(String auth_token) {
        this.authToken = auth_token;
    }





}
