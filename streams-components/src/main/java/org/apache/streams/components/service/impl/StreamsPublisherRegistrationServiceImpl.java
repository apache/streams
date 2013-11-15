package org.apache.streams.components.service.impl;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.streams.components.service.StreamsPublisherRegistrationService;
import org.apache.streams.persistence.model.ActivityStreamsPublisher;
import org.apache.streams.persistence.model.cassandra.CassandraPublisher;
import org.apache.streams.persistence.model.mongo.MongoPublisher;
import org.apache.streams.persistence.repository.PublisherRepository;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class StreamsPublisherRegistrationServiceImpl implements StreamsPublisherRegistrationService {
    private Log log = LogFactory.getLog(StreamsPublisherRegistrationServiceImpl.class);

    private PublisherRepository publisherRepository;
    private Class publisherClass;
    private ObjectMapper mapper;

    @Autowired
    public StreamsPublisherRegistrationServiceImpl(PublisherRepository publisherRepositoryService) {
        this.publisherRepository = publisherRepositoryService;
        this.mapper = new ObjectMapper();
        this.publisherClass = MongoPublisher.class;
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
        ActivityStreamsPublisher publisher = (ActivityStreamsPublisher)mapper.readValue(publisherJSON, publisherClass);

        if (publisher.getSrc() == null) {
            log.info("configuration src is null");
            throw new Exception("configuration src is null");
        }

        ActivityStreamsPublisher fromDb = publisherRepository.getPublisherBySrc(publisher.getSrc());

        if(fromDb != null){
            return fromDb.getInRoute();
        }else{
            publisher.setInRoute("" + UUID.randomUUID());
            publisherRepository.save(publisher);

            return publisher.getInRoute();
        }
    }
}
