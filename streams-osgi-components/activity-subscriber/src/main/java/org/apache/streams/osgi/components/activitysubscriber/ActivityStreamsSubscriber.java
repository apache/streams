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

package org.apache.streams.osgi.components.activitysubscriber;


import java.util.Date;
import java.util.List;

public interface ActivityStreamsSubscriber {
    public void receive(List<String> activity);
    public String getStream();
    public void init();
    public void setInRoute(String route);
    public String getInRoute();
    public void setActivityStreamsSubscriberConfiguration(ActivityStreamsSubscription config);
    public void updateActivityStreamsSubscriberConfiguration(String config);
    public boolean isAuthenticated();
    public void setAuthenticated(boolean authenticated);
    public ActivityStreamsSubscription getActivityStreamsSubscriberConfiguration();
    Date getLastUpdated();
    void setLastUpdated(Date lastUpdated);
}
