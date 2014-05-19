package org.apache.streams.osgi.components.activityconsumer;

/*
 * #%L
 * activity-consumer-bundle [org.apache.streams.osgi.components.activityconsumer]
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


import org.codehaus.jackson.annotate.JsonTypeInfo;

import java.net.URI;


@JsonTypeInfo(use= JsonTypeInfo.Id.CLASS, include=JsonTypeInfo.As.PROPERTY, property="@class")
public interface ActivityConsumer {
    public String receive(String activity);
    public void init();
    public URI getSrc();
    public void setSrc(String src);
    public void setInRoute(String route);
    public String getInRoute();
    public String getAuthToken();
    public void setAuthToken(String token);
    public boolean isAuthenticated();
    public void setAuthenticated(boolean authenticated);
}
