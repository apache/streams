package org.apache.streams.components.activitysubscriber.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.streams.components.activitysubscriber.ActivityStreamsSubscriber;
import org.apache.streams.persistence.model.ActivityStreamsEntry;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.ObjectMapper;

import java.util.*;

public class ActivityStreamsSubscriberDelegate implements ActivityStreamsSubscriber {

    private static final transient Log LOG = LogFactory.getLog(ActivityStreamsSubscriberDelegate.class);

    //an individual subscriber gets ONE stream which is an aggregation of all its SRCs
    private Set<ActivityStreamsEntry> stream;
    private ObjectMapper mapper;
    private Date lastUpdated;

    public ActivityStreamsSubscriberDelegate(){
        this.stream = new TreeSet<ActivityStreamsEntry>(new Comparator<ActivityStreamsEntry>() {
            @Override
            public int compare(ActivityStreamsEntry activityStreamsEntry1, ActivityStreamsEntry activityStreamsEntry2) {
                return -1 * (activityStreamsEntry1.getPublished()).compareTo(activityStreamsEntry2.getPublished());
            }
        });
        this.lastUpdated = new Date(0);
        this.mapper = new ObjectMapper();
        mapper.configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    //return the list of activities (stream) as a json string
    public String getStream() throws Exception{
        List<String> activityEntires = new ArrayList<String>();
        for(ActivityStreamsEntry e:stream){
            activityEntires.add(mapper.writeValueAsString(e));
        }
        return activityEntires.toString();
    }

    public Date getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(Date lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    public void receive(List<ActivityStreamsEntry> activities){
        stream.addAll(activities);
    }
}
