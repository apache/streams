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

public interface ActivityStreamsSubscriptionOutput {

    public String getOutputType();
    public void setOutputType(String outputType);

    public String getMethod();
    public void setMethod(String method);

    public String getUrl();
    public void setUrl(String url);

    public String getDeliveryFrequency();
    public void setDeliveryFrequency(String deliveryFrequency);

    public String getMaxSize();
    public void setMaxSize(int maxSize);

    public String getAuthType();
    public void setAuthType(String authType);

    public String getUsername();
    public void setUsername(String username);

    public String getPassword();
    public void setPassword(String password);

}
