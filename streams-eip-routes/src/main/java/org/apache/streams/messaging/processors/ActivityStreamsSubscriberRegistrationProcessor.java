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

package org.apache.streams.messaging.processors;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.streams.messaging.service.SubscriptionService;
import org.apache.streams.messaging.service.impl.CassandraSubscriptionService;
import org.apache.streams.osgi.components.activitysubscriber.ActivityStreamsSubscription;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.ObjectMapper;


public class ActivityStreamsSubscriberRegistrationProcessor implements Processor{
    private static final transient Log LOG = LogFactory.getLog(ActivityStreamsSubscriberRegistrationProcessor.class);
    private SubscriptionService subscriptionService;

    public ActivityStreamsSubscriberRegistrationProcessor(SubscriptionService subscriptionService){
        this.subscriptionService = subscriptionService;
    }

    public void process(Exchange exchange){
        LOG.info("processing the subscriber...");
        //add the necessary headers to the message so that the activity registration component
        //can do a lookup to either make a new processor and endpoint, or pass the message to the right one
        String httpMethod = exchange.getIn().getHeader("CamelHttpMethod").toString();

        if (!httpMethod.equals("POST")){
            //reject anything that isn't a post...Camel 2.10 solves needing this check, however, SM 4.4 doesn't have the latest
            exchange.getOut().setFault(true);
            exchange.getOut().setHeader(Exchange.HTTP_RESPONSE_CODE,405);
        }  else {

             //for now...just expect a post with a uri in the body...should have some checking here with http response codes
            // authentication, all that good stuff...happens in the registration module


            String body = exchange.getIn().getBody(String.class);

            LOG.info("receiving the subscriber: "+body);
            //OAuth token? What does subscriber post to init a subscription URL?
            //maybe its a list of URLs to subscribe to subscriptions=1,2,3,4&auth_token=XXXX

            ObjectMapper mapper = new ObjectMapper();
            mapper.configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES,false);

            try {

                // read from file, convert it to user class
                ActivityStreamsSubscription configuration = mapper.readValue(body, ActivityStreamsSubscription.class);
                if(configuration.getFilters() == null){
                    configuration.setFilters(subscriptionService.getFilters(configuration.getAuthToken()));
                }else{
                    subscriptionService.saveFilters(configuration);
                }
                exchange.getOut().setBody(configuration);

            } catch (Exception e) {
                LOG.info("exception" + e);
                exchange.getOut().setFault(true);
                exchange.getOut().setHeader(Exchange.HTTP_RESPONSE_CODE,400);
                exchange.getOut().setBody("POST should contain a valid Subscription configuration object.");
            }



            //just pass this on to the route creator, body will be the dedicated URL for this subscriber

        }



    }


}
