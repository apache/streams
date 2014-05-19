package org.apache.streams.osgi.components.activitysubscriber;

/*
 * #%L
 * activity-subscriber-bundle [org.apache.streams.osgi.components.activitysubscriber]
 * %%
 * Copyright (C) 2013 - 2014 Apache Streams Project
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */


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
