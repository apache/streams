package org.apache.streams.components.service.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.streams.components.service.StreamsPublisherRegistrationService;
import org.apache.streams.components.configuration.StreamsConfiguration;
import org.apache.streams.components.activityconsumer.ActivityConsumer;
import org.apache.streams.components.activityconsumer.ActivityConsumerWarehouse;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class StreamsPublisherRegistrationServiceImpl implements StreamsPublisherRegistrationService {
    private Log log = LogFactory.getLog(StreamsPublisherRegistrationServiceImpl.class);

    private ActivityConsumerWarehouse activityConsumerWarehouse;
    private StreamsConfiguration configuration;

    @Autowired
    public StreamsPublisherRegistrationServiceImpl(ActivityConsumerWarehouse activityConsumerWarehouse, StreamsConfiguration configuration) {
        this.activityConsumerWarehouse = activityConsumerWarehouse;
        this.configuration = configuration;
    }

    /**
     * registers the publisher according to the publisherJSON
     * @param publisherJSON the JSON of the publisher to be registered
     * @return a url that the client can use to POST activity streams
     * */
    public String register(String publisherJSON) throws Exception {
        log.info("attempting to register publisher json: " + publisherJSON);
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        // read from file, convert it to user class
        ActivityConsumer activityConsumer = mapper.readValue(publisherJSON, ActivityConsumer.class);

        if (activityConsumer.getSrc() == null) {
            log.info("configuration src is null");
            throw new Exception("configuration src is null");
        }

        activityConsumer.setAuthenticated(true);
        activityConsumer.setInRoute("" + UUID.randomUUID());
        activityConsumerWarehouse.register(activityConsumer);
        return configuration.getBaseUrlPath() + "postActivity/" + activityConsumer.getInRoute();
    }
}
