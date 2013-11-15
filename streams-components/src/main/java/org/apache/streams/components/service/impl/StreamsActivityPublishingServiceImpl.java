package org.apache.streams.components.service.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.streams.components.service.StreamsActivityPublishingService;
import org.apache.streams.components.service.StreamsActivityRepositoryService;
import org.apache.streams.persistence.model.ActivityStreamsPublisher;
import org.apache.streams.persistence.repository.PublisherRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class StreamsActivityPublishingServiceImpl implements StreamsActivityPublishingService {
    private Log log = LogFactory.getLog(StreamsActivityPublishingServiceImpl.class);

    private StreamsActivityRepositoryService activityService;
    private PublisherRepository publisherRepository;

    @Autowired
    public StreamsActivityPublishingServiceImpl(StreamsActivityRepositoryService activityService, PublisherRepository publisherRepository) {
        this.activityService = activityService;
        this.publisherRepository = publisherRepository;
    }

    /**
     * publishes the activity to the activityService for storage
     * @param publisherID the id of the publisher publishing this activity
     * @param activityJSON the activityJSON being published
     * @return a success message if no errors were thrown
     * */
    public String publish(String publisherID, String activityJSON) throws Exception {
        //TODO: this should eventually authenticate
        ActivityStreamsPublisher publisher = publisherRepository.getPublisherByInRoute(publisherID);
        activityService.receiveActivity(activityJSON);
        return activityJSON;
    }
}
