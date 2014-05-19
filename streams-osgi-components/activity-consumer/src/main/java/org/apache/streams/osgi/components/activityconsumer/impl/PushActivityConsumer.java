package org.apache.streams.osgi.components.activityconsumer.impl;

/*
 * #%L
 * activity-consumer-bundle [org.apache.streams.osgi.components.activityconsumer]
 * %%
 * Copyright (C) 2013 - 2014 Apache Streams Project
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

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

    public List<String> split(String activities) {
        LOG.info("I am going to split this message: " + activities);

        List<String> activitiesList = new ArrayList<String>();
        activitiesList.add(activities);
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
