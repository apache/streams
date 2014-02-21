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

package org.apache.streams.messaging.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class EipConfigurator {



    @Value("${consumer.inRouteHost}")
    private String consumerInRouteHost;

    @Value("${consumer.inRoutePort}")
    private String consumerInRoutePort;

    @Value("${subscriber.inRouteHost}")
    private String subscriberInRouteHost;

    @Value("${subscriber.inRoutePort}")
    private String subscriberInRoutePort;


    @Value("${consumer.activityQUri}")
    private String consumerActivityQUri;

    @Value("${consumer.publisherEndpointProtocol}")
    private String publisherEndpointProtocol;

    @Value("${consumer.publisherEndpointUrlResource}")
    private String publisherEndpointUrlResource;

    @Value("${consumer.receiveMethod}")
    private String consumerReceiveMethod;

    @Value("${consumer.splitMethod}")
    private String consumerSplitMethod;

    @Value("${subscriber.subscriberEndpointProtocol}")
    private String subscriberEndpointProtocol;

    @Value("${subscriber.subscriberEndpointUrlResource}")
    private String subscriberEndpointUrlResource;

    @Value("${subscriber.receiveMethod}")
    private String subscriberReceiveMethod;

    @Value("${subscriber.postMethod}")
    private String subscriberPostMethod;

    @Value("${subscriber.getMethod}")
    private String subscriberGetMethod;


    @Value("${servlet.baseUrlPath}")
    private String baseUrlPath;


    public static String ENDPOINT_PROTOCOL_JETTY="jetty:http://";
    public static String ENDPOINT_PROTOCOL_SERVLET="servlet:///";

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

    public String getSubscriberInRouteHost() {
        return subscriberInRouteHost;
    }

    public void setSubscriberInRouteHost(String subscriberInRouteHost) {
        this.subscriberInRouteHost = subscriberInRouteHost;
    }

    public String getSubscriberInRoutePort() {
        return subscriberInRoutePort;
    }

    public void setSubscriberInRoutePort(String subscriberInRoutePort) {
        this.subscriberInRoutePort = subscriberInRoutePort;
    }

    public String getPublisherEndpointProtocol() {
        return publisherEndpointProtocol;
    }

    public void setPublisherEndpointProtocol(String publisherEndpointProtocol) {
        this.publisherEndpointProtocol = publisherEndpointProtocol;
    }

    public String getPublisherEndpointUrlResource() {
        return publisherEndpointUrlResource;
    }

    public void setPublisherEndpointUrlResource(String publisherEndpointUrlResource) {
        this.publisherEndpointUrlResource = publisherEndpointUrlResource;
    }

    public String getConsumerReceiveMethod() {
        return consumerReceiveMethod;
    }

    public void setConsumerReceiveMethod(String consumerReceiveMethod) {
        this.consumerReceiveMethod = consumerReceiveMethod;
    }

    public String getConsumerSplitMethod() {
        return consumerSplitMethod;
    }

    public void setConsumerSplitMethod(String consumerSplitMethod) {
        this.consumerSplitMethod = consumerSplitMethod;
    }

    public String getSubscriberEndpointProtocol() {
        return subscriberEndpointProtocol;
    }

    public void setSubscriberEndpointProtocol(String subscriberEndpointProtocol) {
        this.subscriberEndpointProtocol = subscriberEndpointProtocol;
    }

    public String getSubscriberEndpointUrlResource() {
        return subscriberEndpointUrlResource;
    }

    public void setSubscriberEndpointUrlResource(String subscriberEndpointUrlResource) {
        this.subscriberEndpointUrlResource = subscriberEndpointUrlResource;
    }

    public String getSubscriberReceiveMethod() {
        return subscriberReceiveMethod;
    }

    public void setSubscriberReceiveMethod(String subscriberReceiveMethod) {
        this.subscriberReceiveMethod = subscriberReceiveMethod;
    }

    public String getSubscriberPostMethod() {
        return subscriberPostMethod;
    }

    public void setSubscriberPostMethod(String subscriberPostMethod) {
        this.subscriberPostMethod = subscriberPostMethod;
    }

    public String getSubscriberGetMethod() {
        return subscriberGetMethod;
    }

    public void setSubscriberGetMethod(String subscriberGetMethod) {
        this.subscriberGetMethod = subscriberGetMethod;
    }

    public String getBaseUrlPath() {
        return baseUrlPath;
    }

    public void setBaseUrlPath(String baseUrlPath) {
        this.baseUrlPath = baseUrlPath;
    }


}
