package org.apache.streams.persistence.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;

@Configuration
@PropertySource("classpath:mongo.properties")
public class MongoConfiguration {

    @Bean
    public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
        return new PropertySourcesPlaceholderConfigurer();
    }

    @Value("${mongo.dbName}")
    private String dbName;

    @Value("${mongo.activityStreamsCollectionName}")
    private String activitystreamsCollectionName;

    @Value("${mongo.subscriptionCollectionName}")
    private String subscriptionCollectionName;

    @Value("${mongo.publisherCollectionName}")
    private String publisherCollectionName;

    public String getDbName() {
        return dbName;
    }

    public void setDbName(String dbName) {
        this.dbName = dbName;
    }

    public String getActivitystreamsCollectionName() {
        return activitystreamsCollectionName;
    }

    public void setActivitystreamsCollectionName(String activitystreamsCollectionName) {
        this.activitystreamsCollectionName = activitystreamsCollectionName;
    }

    public String getSubscriptionCollectionName() {
        return subscriptionCollectionName;
    }

    public void setSubscriptionCollectionName(String subscriptionCollectionName) {
        this.subscriptionCollectionName = subscriptionCollectionName;
    }

    public String getPublisherCollectionName() {
        return publisherCollectionName;
    }

    public void setPublisherCollectionName(String publisherCollectionName) {
        this.publisherCollectionName = publisherCollectionName;
    }
}
