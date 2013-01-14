package org.apache.streams.messaging.routers;

import java.lang.Object;
import java.lang.String;
import java.util.Map;
import org.apache.camel.Properties;



public interface ActivityRouter {

    /*
    * Use this method to compute dynamic where we should route next.
    *
    * @param body the message body
    * @return endpoints to go, or <tt>null</tt> to indicate the end
    */
    String slip(String body,@Properties Map<String, Object> properties);
    void addRoutingRules(Map<String,String> rules);

}
