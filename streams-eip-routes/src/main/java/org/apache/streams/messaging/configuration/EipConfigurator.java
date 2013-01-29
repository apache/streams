package org.apache.streams.messaging.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class EipConfigurator {



    @Value("${consumer.inRouteHost}")
    private String consumerInRouteHost;

    @Value("${consumer.inRoutePort}")
    private String consumerInRoutePort;


    @Value("${consumer.activityQUri}")
    private String consumerActivityQUri;

    public static String CONSUMER_URL_RESOURCE = "/streams/publish";
    public static String CONSUMER_RECIEVE_METHOD = "receive";
    public static String CONSUMER_SPLIT_METHOD = "split";

    public String getConsumerInRouteHost() {
        return consumerInRouteHost;
    }

    public String getConsumerInRoutePort() {
        return consumerInRoutePort;
    }

    public String getConsumerActivityQUri() {
        return consumerActivityQUri;
    }

    public void setConsumerActivityQUri(String consumerActivityQUri) {
        this.consumerActivityQUri = consumerActivityQUri;
    }

    public void setConsumerInRoutePort(String consumerInRoutePort) {
        this.consumerInRoutePort = consumerInRoutePort;
    }

    public void setConsumerInRouteHost(String consumerInRouteHost) {
        this.consumerInRouteHost = consumerInRouteHost;
    }



}
