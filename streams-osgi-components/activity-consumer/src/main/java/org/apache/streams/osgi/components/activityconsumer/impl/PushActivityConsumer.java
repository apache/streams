package org.apache.streams.osgi.components.activityconsumer.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.streams.cassandra.model.CassandraActivityStreamsEntry;
import org.apache.streams.osgi.components.activityconsumer.ActivityConsumer;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.ObjectMapper;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.ArrayList;

public class PushActivityConsumer implements ActivityConsumer {

    private static final transient Log LOG = LogFactory.getLog(PushActivityConsumer.class);

    private URI src;


    private String authToken;

    private boolean authenticated;

    private String inRoute;

    public PushActivityConsumer(){
    }


    public URI getSrc() {
        return src;
    }

    public void setSrc(String src) {
        try{
            this.src = new URI(src);

        } catch (URISyntaxException e) {
           this.src=null;
        }
    }

    public String getAuthToken() {
        return authToken;
    }

    public void setAuthToken(String authToken) {
        this.authToken = authToken;
    }

    public boolean isAuthenticated() {
        return authenticated;
    }

    public void setAuthenticated(boolean authenticated) {
        this.authenticated = authenticated;
    }

    public String receive (String activity){
        //receive activities...do anything that is necessary
        LOG.info("a message I published: " + activity);
        return activity;
        //pass off to activity splitter

    }

    public List<CassandraActivityStreamsEntry> split(String activities) {
        LOG.info("I am going to split this message: " + activities);
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        List<CassandraActivityStreamsEntry> activitiesList = new ArrayList<CassandraActivityStreamsEntry>();

        try {
            //attempt to convert the activity to a java object
            LOG.info("About to preform the translation to JSON Object");
            CassandraActivityStreamsEntry streamsEntry = mapper.readValue(activities, CassandraActivityStreamsEntry.class);
            activitiesList.add(streamsEntry);
        } catch (Exception e) {
            LOG.info("Error while converting the JSON object to POJO and saving to database",e);
        }
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
