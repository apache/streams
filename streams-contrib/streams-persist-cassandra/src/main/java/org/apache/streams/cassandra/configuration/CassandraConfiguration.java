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

package org.apache.streams.cassandra.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class CassandraConfiguration {
    @Value("${keyspaceName}")
    private String keyspaceName;

    @Value("${activitystreamsColumnFamilyName}")
    private String activitystreamsColumnFamilyName;

    @Value("${subscriptionColumnFamilyName}")
    private String subscriptionColumnFamilyName;

    @Value("${publisherColumnFamilyName}")
    private String publisherColumnFamilyName;

    @Value("${cassandraPort}")
    private String cassandraPort;

    public String getKeyspaceName() {
        return keyspaceName;
    }

    public void setKeyspaceName(String keyspaceName) {
        this.keyspaceName = keyspaceName;
    }

    public String getActivitystreamsColumnFamilyName() {
        return activitystreamsColumnFamilyName;
    }

    public void setActivitystreamsColumnFamilyName(String activitystreamsColumnFamilyName) {
        this.activitystreamsColumnFamilyName = activitystreamsColumnFamilyName;
    }

    public String getSubscriptionColumnFamilyName() {
        return subscriptionColumnFamilyName;
    }

    public void setSubscriptionColumnFamilyName(String subscriptionColumnFamilyName) {
        this.subscriptionColumnFamilyName = subscriptionColumnFamilyName;
    }

    public String getPublisherColumnFamilyName() {
        return publisherColumnFamilyName;
    }

    public void setPublisherColumnFamilyName(String publisherColumnFamilyName) {
        this.publisherColumnFamilyName = publisherColumnFamilyName;
    }

    public String getCassandraPort() {
        return cassandraPort;
    }

    public void setCassandraPort(String cassandraPort) {
        this.cassandraPort = cassandraPort;
    }
}

