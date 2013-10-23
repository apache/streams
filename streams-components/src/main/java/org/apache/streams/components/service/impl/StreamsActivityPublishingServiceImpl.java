package org.apache.streams.components.service.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.streams.components.service.StreamsActivityPublishingService;
import org.apache.streams.components.service.StreamsActivityRepositoryService;
import org.apache.streams.components.service.StreamsPublisherRepositoryService;
import org.apache.streams.persistence.model.ActivityStreamsPublisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class StreamsActivityPublishingServiceImpl implements StreamsActivityPublishingService {
    private Log log = LogFactory.getLog(StreamsActivityPublishingServiceImpl.class);

    private StreamsActivityRepositoryService activityService;
    private StreamsPublisherRepositoryService publisherService;

    @Autowired
    public StreamsActivityPublishingServiceImpl(StreamsActivityRepositoryService activityService, StreamsPublisherRepositoryService publisherService) {
        this.activityService = activityService;
        this.publisherService = publisherService;
    }

    /**
     * publishes the activity to the activityService for storage
     * @param publisherID the id of the publisher publishing this activity
     * @param activityJSON the activityJSON being published
     * @return a success message if no errors were thrown
     * */
    public String publish(String publisherID, String activityJSON) throws Exception {
        ActivityStreamsPublisher publisher = publisherService.getActivityStreamsPublisher(publisherID);
        activityService.receiveActivity(publisher,activityJSON);
        return activityJSON;
    }
}
