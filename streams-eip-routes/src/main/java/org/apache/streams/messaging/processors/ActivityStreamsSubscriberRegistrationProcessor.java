package org.apache.streams.messaging.processors;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.commons.logging.impl.SimpleLog;
import org.apache.streams.osgi.components.activitysubscriber.ActivityStreamsSubscriberConfiguration;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;


public class ActivityStreamsSubscriberRegistrationProcessor implements Processor{

    public void process(Exchange exchange){
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

            //OAuth token? What does subscriber post to init a subscription URL?
            //maybe its a list of URLs to subscribe to subscriptions=1,2,3,4&auth_token=XXXX

            ObjectMapper mapper = new ObjectMapper();

            try {
                // read from file, convert it to user class
                ActivityStreamsSubscriberConfiguration configuration = mapper.readValue(body, ActivityStreamsSubscriberConfiguration.class);
                exchange.getOut().setBody(configuration);

            } catch (Exception e) {
                exchange.getOut().setFault(true);
                exchange.getOut().setHeader(Exchange.HTTP_RESPONSE_CODE,400);
                exchange.getOut().setBody("POST should contain a valid Subscription configuration object.");
            }



            //just pass this on to the route creator, body will be the dedicated URL for this subscriber

        }



    }


}
