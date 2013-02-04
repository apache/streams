package org.apache.streams.messaging.processors;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;


public class ActivityPublisherRegistrationProcessor implements Processor{

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
            try{
                URI publisherUrl = new URI(body);
                exchange.getOut().setHeader("activityPublisherUri",body);
                exchange.getOut().setBody(body);
            }catch(URISyntaxException e){
                exchange.getOut().setFault(true);
                exchange.getOut().setHeader(Exchange.HTTP_RESPONSE_CODE,400);
                exchange.getOut().setBody("POST should only contain a valid URI that is registering as an Activity Publisher.");
            }
        }



    }
}
