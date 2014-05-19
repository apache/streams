package org.apache.streams.osgi.components.activitysubscriber.impl;

/*
 * #%L
 * activity-subscriber-bundle [org.apache.streams.osgi.components.activitysubscriber]
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
import org.apache.streams.osgi.components.activitysubscriber.ActivityStreamsSubscriber;
import org.apache.streams.osgi.components.activitysubscriber.ActivityStreamsSubscription;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.ObjectMapper;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ActivityStreamsSubscriberDelegate implements ActivityStreamsSubscriber {

    private static final transient Log LOG = LogFactory.getLog(ActivityStreamsSubscriberDelegate.class);

    private boolean authenticated;

    private ActivityStreamsSubscription activityStreamsSubscriberConfiguration;

    private String inRoute;

    //an individual subscriber gets ONE stream which is an aggregation of all its SRCs
    private List<String> stream;

    private Date lastUpdated;


    public ActivityStreamsSubscriberDelegate(ActivityStreamsSubscription configuration){
        setActivityStreamsSubscriberConfiguration(configuration);
        stream = new ArrayList<String>();
        lastUpdated = new Date(0);
    }


    public ActivityStreamsSubscription getActivityStreamsSubscriberConfiguration() {
        return activityStreamsSubscriberConfiguration;
    }

    public void setActivityStreamsSubscriberConfiguration(ActivityStreamsSubscription activityStreamsSubscriberConfiguration) {
        this.activityStreamsSubscriberConfiguration = activityStreamsSubscriberConfiguration;
    }

    public void updateActivityStreamsSubscriberConfiguration(String activityStreamsSubscriberConfiguration) {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES,false);

        try {
            // read from file, convert it to user class
            ActivityStreamsSubscription configuration = mapper.readValue(activityStreamsSubscriberConfiguration, ActivityStreamsSubscriptionImpl.class);
            this.activityStreamsSubscriberConfiguration = configuration;

        } catch (Exception e) {
            LOG.info("exception" + e);

        }

    }

    public boolean isAuthenticated() {
        return authenticated;
    }

    public void setAuthenticated(boolean authenticated) {
        this.authenticated = authenticated;
    }

    public String getInRoute() {
        return inRoute;
    }

    public void setInRoute(String inRoute) {
        this.inRoute = inRoute;
    }

    public void receive (List<String> activity){
        //add new activities to stream
        LOG.info("adding activities to subscription stream");
        stream.addAll(0,activity);
    }

    //return the list of activities (stream) as a json string
    public String getStream() {

        return stream.toString();
    }

    public Date getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(Date lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    public void init(){
        //any initialization... gets called directly after registration



    }





}
