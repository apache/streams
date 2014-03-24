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

