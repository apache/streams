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

package org.apache.streams.messaging.routers.impl;


import org.apache.camel.builder.RouteBuilder;
import org.apache.streams.messaging.routers.ActivityConsumerRouteBuilder;


import org.apache.streams.osgi.components.activityconsumer.ActivityConsumerWarehouse;
import org.apache.streams.osgi.components.activityconsumer.ActivityConsumer;
import org.apache.streams.messaging.configuration.EipConfigurator;
import org.springframework.beans.factory.annotation.Autowired;
import org.apache.camel.Exchange;
import org.apache.camel.CamelContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.UUID;


public class ActivityConsumerRouter extends RouteBuilder implements ActivityConsumerRouteBuilder {
    private static final transient Log LOG = LogFactory.getLog(ActivityConsumerRouter.class);

    @Autowired
    private EipConfigurator configuration;

    protected CamelContext camelContext;

    private ActivityConsumerWarehouse activityConsumerWarehouse;

    public void setCamelContext(CamelContext camelContext) {
        this.camelContext = camelContext;
    }

    public void setActivityConsumerWarehouse(ActivityConsumerWarehouse activityConsumerWarehouse) {
        this.activityConsumerWarehouse = activityConsumerWarehouse;
    }


    public void createNewRouteForConsumer(Exchange exchange, ActivityConsumer activityConsumer){

        //todo: add some better scheme then getCount for URL...
        //todo: make the route again if consumer exists...and context doesn't have route
        if (activityConsumer.isAuthenticated()){
                ActivityConsumer existingConsumer = activityConsumerWarehouse.findConsumerBySrc(activityConsumer.getSrc().toASCIIString());

                if (existingConsumer==null){

                  try{

                    if (configuration.getPublisherEndpointProtocol().equals(EipConfigurator.ENDPOINT_PROTOCOL_JETTY)){
                        activityConsumer.setInRoute(configuration.getConsumerInRouteHost()+ ":" + configuration.getConsumerInRoutePort() +"/" + configuration.getPublisherEndpointUrlResource() + "/" + UUID.randomUUID());
                        //set the body to the url the producer should post to
                        exchange.getOut().setBody("http://" + activityConsumer.getInRoute());
                    }else if (configuration.getPublisherEndpointProtocol().equals(EipConfigurator.ENDPOINT_PROTOCOL_SERVLET)){
                        activityConsumer.setInRoute( configuration.getPublisherEndpointUrlResource() + "/" + UUID.randomUUID());
                        //set the body to the url the producer should post to
                        exchange.getOut().setBody(configuration.getBaseUrlPath() + activityConsumer.getInRoute());
                    } else{
                        throw new Exception("No supported endpoint protocol is configured.");
                    }


                        //setup a message queue for this consumer.getInRoute()
                        camelContext.addRoutes(new DynamicConsumerRouteBuilder(configuration,camelContext, configuration.getPublisherEndpointProtocol() + activityConsumer.getInRoute(), activityConsumer));


                        LOG.info("all messages sent from " + activityConsumer.getSrc() + " must be posted to " + activityConsumer.getInRoute());
                        //only add the route to the warehouse after its been created in messaging system...
                        activityConsumerWarehouse.register(activityConsumer);
                    }catch (Exception e){
                        exchange.getOut().setHeader(Exchange.HTTP_RESPONSE_CODE,500);
                        exchange.getOut().setBody("error creating route: " + e);
                        LOG.error("error creating route: " + e);
                    }

                } else{

                    exchange.getOut().setBody(configuration.getBaseUrlPath() + existingConsumer.getInRoute());
                }

        }else{
                exchange.getOut().setFault(true);
                exchange.getOut().setHeader(Exchange.HTTP_RESPONSE_CODE,401);
                exchange.getOut().setBody("Authentication failed.");
        }

    }


    public void configure() throws java.lang.Exception{
        //nothing...set the context?

    }

    /**
     * This route builder is a skeleton to add new routes at runtime
     */
    private static final class DynamicConsumerRouteBuilder extends RouteBuilder {
        private final String from;
        private ActivityConsumer activityConsumer;


        private EipConfigurator configuration;

        private DynamicConsumerRouteBuilder(EipConfigurator configuration, CamelContext context, String from, ActivityConsumer activityConsumer) {
            super(context);
            this.from = from;
            this.activityConsumer = activityConsumer;
            this.configuration = configuration;
        }

        @Override
        public void configure() throws Exception {


            from(from)
                    .bean(activityConsumer, configuration.getConsumerReceiveMethod()).setBody(body())
                    .split()
                    .method(activityConsumer, configuration.getConsumerSplitMethod())
                    .to(configuration.getConsumerActivityQUri());


        }
    }

}
