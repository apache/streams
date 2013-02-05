package org.apache.streams.messaging.processors;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.streams.osgi.components.activityconsumer.ActivityConsumer;
import org.apache.streams.osgi.components.activityconsumer.impl.PushActivityConsumer;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.ObjectMapper;


public class ActivityPublisherRegistrationProcessor implements Processor{
    private static final transient Log LOG = LogFactory.getLog(ActivityStreamsSubscriberRegistrationProcessor.class);
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
            ObjectMapper mapper = new ObjectMapper();
            mapper.configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES,false);

            try {

                // read from file, convert it to user class
                ActivityConsumer configuration = mapper.readValue(body, ActivityConsumer.class);
                if (configuration.getSrc()==null){
                   LOG.info("configuration src is null");
                   throw new Exception();
                }

                exchange.getOut().setBody(configuration);

            } catch (Exception e) {
                LOG.info("error: " + e);
                exchange.getOut().setFault(true);
                exchange.getOut().setHeader(Exchange.HTTP_RESPONSE_CODE,400);
                exchange.getOut().setBody("POST should contain a valid JSON configuration for registering as an Activity Publisher (check that src element is valid).");
            }
        }



    }
}
