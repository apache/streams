package org.apache.streams.messaging.routers.impl;


import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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


      //  ActivityStreamsSubscriber existingConsumer = activityStreamsSubscriberWarehouse.findSubscriberBySrc(activityStreamsSubscriber.getSrc());



            activityStreamsSubscriber.setInRoute("http://" + configuration.getSubscriberInRouteHost()+ ":" + configuration.getSubscriberInRoutePort() + EipConfigurator.SUBSCRIBER_URL_RESOURCE + "/" + UUID.randomUUID());


            try{

                //setup a message queue for this consumer.getInRoute()
                camelContext.addRoutes(new DynamicSubscriberRouteBuilder(configuration,camelContext, "jetty:" + activityStreamsSubscriber.getInRoute(), activityStreamsSubscriber));
                //set the body to the url the producer should post to
                exchange.getOut().setBody(activityStreamsSubscriber.getInRoute());
                log.info("subs : " + activityStreamsSubscriber.getSubscriptions());
                activityStreamsSubscriber.setActivityStreamsSubscriberWarehouse(activityStreamsSubscriberWarehouse);
                //only add the route to the warehouse after its been created in messaging system...
               activityStreamsSubscriberWarehouse.register(activityStreamsSubscriber.getSubscriptions(),activityStreamsSubscriber);
            }catch (Exception e){
                exchange.getOut().setHeader(Exchange.HTTP_RESPONSE_CODE,500);
                exchange.getOut().setBody("error creating route: " + e);
                LOG.error("error creating route: " + e);
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
                            .bean(activityStreamsSubscriber, EipConfigurator.SUBSCRIBER_POST_METHOD).setBody(body())
                        .when(header("CamelHttpMethod").isEqualTo("GET"))
                                // when its a GET it goes to getStream()
                            .bean(activityStreamsSubscriber, EipConfigurator.SUBSCRIBER_GET_METHOD) ;



        }
    }

}
