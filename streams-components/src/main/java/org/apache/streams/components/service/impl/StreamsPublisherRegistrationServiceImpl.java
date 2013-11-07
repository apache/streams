package org.apache.streams.components.service.impl;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.streams.components.service.StreamsPublisherRegistrationService;
import org.apache.streams.components.service.StreamsPublisherRepositoryService;
import org.apache.streams.persistence.model.ActivityStreamsPublisher;
import org.apache.streams.persistence.model.cassandra.CassandraPublisher;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class StreamsPublisherRegistrationServiceImpl implements StreamsPublisherRegistrationService {
    private Log log = LogFactory.getLog(StreamsPublisherRegistrationServiceImpl.class);

    private StreamsPublisherRepositoryService publisherRepositoryService;
    private ObjectMapper mapper;

    @Autowired
    public StreamsPublisherRegistrationServiceImpl(StreamsPublisherRepositoryService publisherRepositoryService) {
        this.publisherRepositoryService = publisherRepositoryService;
        this.mapper = new ObjectMapper();
        mapper.configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    /**
     * registers the publisher according to the publisherJSON
     * @param publisherJSON the JSON of the publisher to be registered
     * @return a url that the client can use to POST activity streams
     * */
    public String register(String publisherJSON) throws Exception {
        log.info("attempting to register publisher json: " + publisherJSON);

        // read from file, convert it to user class
        ActivityStreamsPublisher publisher = mapper.readValue(publisherJSON, CassandraPublisher.class);

        if (publisher.getSrc() == null) {
            log.info("configuration src is null");
            throw new Exception("configuration src is null");
        }

        ActivityStreamsPublisher fromDb = publisherRepositoryService.getActivityStreamsPublisherBySrc(publisher.getSrc());

        if(fromDb != null){
            return fromDb.getInRoute();
        }else{
            publisher.setInRoute("" + UUID.randomUUID());
            publisherRepositoryService.savePublisher(publisher);

            return publisher.getInRoute();
        }
    }
}
