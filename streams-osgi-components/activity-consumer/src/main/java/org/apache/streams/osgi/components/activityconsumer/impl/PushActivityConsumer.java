package org.apache.streams.osgi.components.activityconsumer.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.streams.osgi.components.activityconsumer.ActivityConsumer;
import java.util.List;
import java.util.ArrayList;

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


    public String receive (String activity){
        //receive activities...do anything that is necessary
        LOG.info("got a message i subscribed to: " + activity);
        return activity;
        //pass off to activity splitter

    }

    public List<String> split(String activities){
        LOG.info("I am going to split this message: " + activities);
        ArrayList<String> activitiesList = new ArrayList<String>();
        activitiesList.add("this");
        activitiesList.add("is");
        activitiesList.add("my");
        activitiesList.add("split");
        activitiesList.add("message");
        return activitiesList;
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
