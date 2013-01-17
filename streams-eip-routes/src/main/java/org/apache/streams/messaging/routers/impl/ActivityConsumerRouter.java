package org.apache.streams.messaging.routers.impl;


import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.model.RouteDefinition;
import org.apache.streams.messaging.routers.ActivityRouter;

import org.apache.streams.messaging.rules.impl.SimpleRoutingRule;
import org.apache.camel.DynamicRouter;
import org.apache.streams.osgi.components.activityconsumer.ActivityConsumerWarehouse;
import org.apache.streams.osgi.components.activityconsumer.ActivityConsumer;
import org.springframework.beans.factory.annotation.Autowired;
import org.apache.camel.Header;
import org.apache.camel.Exchange;
import org.apache.camel.CamelContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.camel.ExchangePattern;



public class ActivityConsumerRouter extends RouteBuilder implements ActivityRouter {
    private static final transient Log LOG = LogFactory.getLog(ActivityConsumerRouter.class);


    protected CamelContext camelContext;

    private ActivityConsumerWarehouse activityConsumerWarehouse;

    public void setCamelContext(CamelContext camelContext) {
        this.camelContext = camelContext;
    }

    public void setActivityConsumerWarehouse(ActivityConsumerWarehouse activityConsumerWarehouse) {
        this.activityConsumerWarehouse = activityConsumerWarehouse;
    }


    public void createNewRouteForConsumer(ActivityConsumer activityConsumer){

        //todo: understand if direct protocol is what we want...might need to be configurable
        activityConsumer.setInRoute("direct:" + activityConsumer.getSrc());
        activityConsumerWarehouse.register(activityConsumer);

        try{
            //setup a message queue for this consumer.getInRoute()
            camelContext.addRoutes(new DynamcConsumerRouteBuilder(camelContext, activityConsumer.getInRoute(), activityConsumer));

            LOG.info("all messages sent from " + activityConsumer.getSrc() + " will go to " + activityConsumer.getInRoute());
        }catch (Exception e){
            LOG.error("error creating route: " + e);
        }

    }

    @DynamicRouter
    public String slip(Exchange exchange, String body, @Header("SRC") String src){
            //if not sent from a SRC, kill the routing chain
            if (src==null){
                LOG.info("end of route reached, the body is: " + body);
                exchange.getOut().setBody(null);
                return src;

            }
            LOG.info("body of message at router: " + body);
            //lookup the route to the activityconsumer registered to receive messages from SRC
            String outRoute = activityConsumerWarehouse.findConsumerBySrc(src).getInRoute();
            //for now, just one route out...set SRC to null
            exchange.getOut().setHeader("SRC", null);
            exchange.getOut().setBody(body);
            return outRoute;

    }

    public void configure() throws java.lang.Exception{
        //nothing...set the context?

    }

    /**
     * This route builder is a skeleton to add new routes at runtime
     */
    private static final class DynamcConsumerRouteBuilder extends RouteBuilder {
        private final String from;
        private ActivityConsumer activityConsumer;

        private DynamcConsumerRouteBuilder(CamelContext context, String from, ActivityConsumer activityConsumer) {
            super(context);
            this.from = from;
            this.activityConsumer = activityConsumer;
        }

        @Override
        public void configure() throws Exception {

            //todo: this message to the bean is always NULL!!!
            from(from).bean(activityConsumer, "receive").split().method(activityConsumer, "split").to("direct:activityQ");


        }
    }

}
