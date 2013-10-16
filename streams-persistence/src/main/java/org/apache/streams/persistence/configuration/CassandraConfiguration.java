package org.apache.streams.persistence.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;

@Configuration
@PropertySource("classpath:cassandra.properties")
public class CassandraConfiguration {

    @Bean
    public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
        return new PropertySourcesPlaceholderConfigurer();
    }

    @Value("${cassandra.keyspaceName}")
    private String keyspaceName;

    @Value("${cassandra.activitystreamsColumnFamilyName}")
    private String activitystreamsColumnFamilyName;

    @Value("${cassandra.subscriptionColumnFamilyName}")
    private String subscriptionColumnFamilyName;

    @Value("${cassandra.publisherColumnFamilyName}")
    private String publisherColumnFamilyName;

    @Value("${cassandra.cassandraPort}")
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

