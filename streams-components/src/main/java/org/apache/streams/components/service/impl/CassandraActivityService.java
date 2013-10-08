package org.apache.streams.components.service.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.rave.model.ActivityStreamsEntry;
import org.apache.streams.components.service.StreamsActivityRepositoryService;
import org.apache.streams.persistence.model.cassandra.CassandraActivityStreamsEntry;
import org.apache.streams.persistence.repository.ActivityStreamsRepository;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

@Component
public class CassandraActivityService implements StreamsActivityRepositoryService {

    private static final transient Log LOG = LogFactory.getLog(CassandraActivityService.class);

    private ActivityStreamsRepository activityStreamsRepository;
    private ObjectMapper mapper;

    @Autowired
    public CassandraActivityService(ActivityStreamsRepository activityStreamsRepository) {
        this.activityStreamsRepository = activityStreamsRepository;
        this.mapper = new ObjectMapper();
        mapper.configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    @Override
    public void receiveActivity(String activityJSON) throws IOException {
        ActivityStreamsEntry streamsEntry = mapper.readValue(activityJSON, CassandraActivityStreamsEntry.class);
        streamsEntry.setPublished(new Date());
        activityStreamsRepository.save(streamsEntry);
    }

    @Override
    public List<String> getActivitiesForFilters(List<String> filters, Date lastUpdated) {
        List<ActivityStreamsEntry> activityObjects = activityStreamsRepository.getActivitiesForFilters(filters, lastUpdated);
        Collections.sort(activityObjects, Collections.reverseOrder());
        //TODO: make the number of streams returned configurable
        //TODO: what happens if this .subList(0,0)?
        return getJsonList(activityObjects.subList(0, Math.min(activityObjects.size(), 10)));
    }

    private List<String> getJsonList(List<ActivityStreamsEntry> activities) {
        List<String> jsonList = new ArrayList<String>();
        for (ActivityStreamsEntry entry : activities) {
            try {
                jsonList.add(mapper.writeValueAsString(entry));
            } catch (IOException e) {
                LOG.error("There was an error while trying to convert the java object to a string: " + entry, e);
            }
        }
        return jsonList;
    }
}
