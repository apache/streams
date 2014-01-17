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


import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.streams.messaging.aggregation.ActivityAggregator;
import org.apache.streams.messaging.configuration.EipConfigurator;
import org.apache.streams.messaging.routers.ActivityStreamsSubscriberRouteBuilder;
import org.apache.streams.osgi.components.activitysubscriber.ActivityStreamsSubscriber;
import org.apache.streams.osgi.components.activitysubscriber.ActivityStreamsSubscriberWarehouse;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;
import java.util.UUID;


public class ActivityStreamsSubscriberRouter extends RouteBuilder implements ActivityStreamsSubscriberRouteBuilder {
    private static final transient Log LOG = LogFactory.getLog(ActivityStreamsSubscriberRouter.class);

    @Autowired
    private EipConfigurator configuration;

    protected CamelContext camelContext;

    @Autowired
    private ActivityAggregator activityAggregator;

    private ActivityStreamsSubscriberWarehouse activityStreamsSubscriberWarehouse;

    public void setCamelContext(CamelContext camelContext) {
        this.camelContext = camelContext;
    }

    public void setActivityStreamsSubscriberWarehouse(ActivityStreamsSubscriberWarehouse activityStreamsSubscriberWarehouse) {
        this.activityStreamsSubscriberWarehouse = activityStreamsSubscriberWarehouse;
    }


    public void createNewRouteForSubscriber(Exchange exchange, ActivityStreamsSubscriber activityStreamsSubscriber){

        //todo: add some better scheme then getCount for URL...
        //todo: make the route again if subscriber exists...and context doesn't have route
        if (activityStreamsSubscriber.isAuthenticated()){

            try{

                if (configuration.getSubscriberEndpointProtocol().equals(EipConfigurator.ENDPOINT_PROTOCOL_JETTY)){
                    activityStreamsSubscriber.setInRoute(configuration.getSubscriberInRouteHost()+ ":" + configuration.getSubscriberInRoutePort() +"/" + configuration.getSubscriberEndpointUrlResource() + "/" + UUID.randomUUID());
                    //set the body to the url the producer should post to
                    exchange.getOut().setBody("http://" + activityStreamsSubscriber.getInRoute());
                }else if (configuration.getSubscriberEndpointProtocol().equals(EipConfigurator.ENDPOINT_PROTOCOL_SERVLET)){
                    activityStreamsSubscriber.setInRoute( configuration.getSubscriberEndpointUrlResource() + "/" + UUID.randomUUID());
                    //set the body to the url the producer should post to
                    exchange.getOut().setBody(configuration.getBaseUrlPath() + activityStreamsSubscriber.getInRoute());
                } else{
                    throw new Exception("No supported endpoint protocol is configured.");
                }

                //setup a message queue for this consumer.getInRoute()
                camelContext.addRoutes(new DynamicSubscriberRouteBuilder(configuration,camelContext, configuration.getSubscriberEndpointProtocol() + activityStreamsSubscriber.getInRoute(), activityStreamsSubscriber));

                activityAggregator.updateSubscriber(activityStreamsSubscriber);
                activityStreamsSubscriberWarehouse.register(activityStreamsSubscriber);
            }catch (Exception e){
                exchange.getOut().setHeader(Exchange.HTTP_RESPONSE_CODE,500);
                exchange.getOut().setBody("error creating route: " + e);
                LOG.error("error creating route: " + e);
            }

        }else{
            exchange.getOut().setFault(true);
            exchange.getOut().setHeader(Exchange.HTTP_RESPONSE_CODE,401);
            exchange.getOut().setBody("Authentication failed.");
        }

    }




    public void configure() throws Exception{
        //nothing...set the context?

    }

    /**
     * This route builder is a skeleton to add new routes at runtime
     */
    private static final class DynamicSubscriberRouteBuilder extends RouteBuilder {
        private final String from;
        private ActivityStreamsSubscriber activityStreamsSubscriber;


        private EipConfigurator configuration;

        private DynamicSubscriberRouteBuilder(EipConfigurator configuration, CamelContext context, String from, ActivityStreamsSubscriber activityStreamsSubscriber) {
            super(context);
            this.from = from;
            this.activityStreamsSubscriber = activityStreamsSubscriber;
            this.configuration = configuration;
        }

        @Override
        public void configure() throws Exception {


            from(from)
                    .choice()
                        .when(header("CamelHttpMethod").isEqualTo("POST"))
                            //when its a post...it goes to adding a new src
                            .bean(activityStreamsSubscriber, configuration.getSubscriberPostMethod()).setBody(body())
                        .when(header("CamelHttpMethod").isEqualTo("GET"))
                                // when its a GET it goes to getStream()
                            .bean(activityStreamsSubscriber, configuration.getSubscriberGetMethod()) ;



        }
    }

}
