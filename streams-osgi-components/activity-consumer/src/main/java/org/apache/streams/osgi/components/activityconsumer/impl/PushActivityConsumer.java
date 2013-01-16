package org.apache.streams.osgi.components.activityconsumer.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.streams.osgi.components.activityconsumer.ActivityConsumer;

public class PushActivityConsumer implements ActivityConsumer {

    private static final transient Log LOG = LogFactory.getLog(PushActivityConsumer.class);

    private String src;


    private String inRoute;

    public PushActivityConsumer(String src){
        this.setSrc(src);
    }

    public String getSrc() {
        return src;
    }

    public void setSrc(String src) {
        this.src = src;
    }

    public void receive (Object activity){
        //receive activity...do anything that is necessary
        LOG.info("got a message i subscribed to: " + activity);
        //pass off to activity splitter ?
    }

    public void init(){
        //any initialization...
    }

    public String getInRoute() {
        return inRoute;
    }

    public void setInRoute(String inRoute) {
        this.inRoute = inRoute;
    }

}
