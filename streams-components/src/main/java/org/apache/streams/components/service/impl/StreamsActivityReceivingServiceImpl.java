package org.apache.streams.components.service.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.streams.components.activitysubscriber.ActivityStreamsSubscriberWarehouse;
import org.apache.streams.components.service.StreamsActivityReceivingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class StreamsActivityReceivingServiceImpl implements StreamsActivityReceivingService {
    private Log log = LogFactory.getLog(StreamsActivityReceivingServiceImpl.class);

    private ActivityStreamsSubscriberWarehouse activityStreamsSubscriberWarehouse;

    @Autowired
    public StreamsActivityReceivingServiceImpl(ActivityStreamsSubscriberWarehouse activityStreamsSubscriberWarehouse){
        this.activityStreamsSubscriberWarehouse = activityStreamsSubscriberWarehouse;
    }

    /**
     * looks up the subscriber by the id and returns the stream in string form
     * @param subscriberID the id of the subscriber to look up
     * @return the stream list in string form
     * */
    public  String getActivity(String subscriberID) throws Exception{
        return activityStreamsSubscriberWarehouse.getStream(subscriberID);
    }
}
