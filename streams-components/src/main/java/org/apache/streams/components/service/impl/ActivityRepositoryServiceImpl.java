package org.apache.streams.components.service.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.streams.components.service.StreamsActivityRepositoryService;
import org.apache.streams.persistence.model.ActivityStreamsEntry;
import org.apache.streams.persistence.model.ActivityStreamsPublisher;
import org.apache.streams.persistence.model.cassandra.CassandraActivityStreamsEntry;
import org.apache.streams.persistence.model.mongo.MongoActivityStreamsEntry;
import org.apache.streams.persistence.repository.ActivityStreamsRepository;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class ActivityRepositoryServiceImpl implements StreamsActivityRepositoryService {

    private static final transient Log LOG = LogFactory.getLog(ActivityRepositoryServiceImpl.class);

    private ActivityStreamsRepository activityStreamsRepository;
    private Class activityClass;
    private ObjectMapper mapper;

    @Autowired
    public ActivityRepositoryServiceImpl(ActivityStreamsRepository activityStreamsRepository, Class activityClass) {
        this.activityStreamsRepository = activityStreamsRepository;
        this.activityClass = activityClass;
        this.mapper = new ObjectMapper();
        mapper.configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    @Override
    public void receiveActivity(String activityJSON) throws Exception {
        ActivityStreamsEntry streamsEntry = (ActivityStreamsEntry)mapper.readValue(activityJSON, activityClass);
        streamsEntry.setPublished(new Date());
        streamsEntry.setId(""+UUID.randomUUID());
        activityStreamsRepository.save(streamsEntry);
    }

    @Override
    public List<ActivityStreamsEntry> getActivitiesForFilters(Set<String> filters, Date lastUpdated){
        return activityStreamsRepository.getActivitiesForFilters(filters, lastUpdated);
    }
}
