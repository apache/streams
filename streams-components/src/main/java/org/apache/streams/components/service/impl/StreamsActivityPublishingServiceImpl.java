package org.apache.streams.components.service.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.streams.components.service.StreamsActivityPublishingService;
import org.apache.streams.components.service.StreamsActivityRepositoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class StreamsActivityPublishingServiceImpl implements StreamsActivityPublishingService {
    private Log log = LogFactory.getLog(StreamsActivityPublishingServiceImpl.class);

    private StreamsActivityRepositoryService activityService;

    @Autowired
    public StreamsActivityPublishingServiceImpl(StreamsActivityRepositoryService activityService) {
        this.activityService = activityService;
    }

    /**
     * publishes the activity to the activityService for storage
     * @param publisherID the id of the publisher publishing this activity
     * @param activityJSON the activityJSON being published
     * @return a success message if no errors were thrown
     * */
    public String publish(String publisherID, String activityJSON) throws IOException {
        activityService.receiveActivity(activityJSON);
        return "The activity was successfully published!";
    }
}
