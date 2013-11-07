package org.apache.streams.components.service.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.streams.components.service.StreamsActivityRepositoryService;
import org.apache.streams.persistence.model.ActivityStreamsEntry;
import org.apache.streams.persistence.model.ActivityStreamsPublisher;
import org.apache.streams.persistence.model.cassandra.CassandraActivityStreamsEntry;
import org.apache.streams.persistence.repository.ActivityStreamsRepository;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

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
    public void receiveActivity(ActivityStreamsPublisher publisher, String activityJSON) throws Exception {
        ActivityStreamsEntry streamsEntry = mapper.readValue(activityJSON, CassandraActivityStreamsEntry.class);
        if(!publisher.getSrc().equals(streamsEntry.getProvider().getUrl())){
            throw new Exception("The Publisher source: "+ publisher.getSrc() +" and Activity Provider source: " + streamsEntry.getProvider().getUrl() + " were not equal");
        }
        streamsEntry.setPublished(new Date());
        streamsEntry.setId(""+UUID.randomUUID());
        activityStreamsRepository.save(streamsEntry);
    }

    @Override
    public List<String> getActivitiesForTags(Set<String> tags, Date lastUpdated){
        List<ActivityStreamsEntry> activityObjects = activityStreamsRepository.getActivitiesForTags(tags, lastUpdated);
        Collections.sort(activityObjects, Collections.reverseOrder());
        //TODO: make the number of streams returned configurable
        return getJsonList(activityObjects.subList(0, Math.min(activityObjects.size(), 10)));
    }

    private List<String> getJsonList(List<ActivityStreamsEntry> activities) {
        List<String> jsonList = new ArrayList<String>();
        for (ActivityStreamsEntry entry : activities) {
            try {
                jsonList.add(mapper.writeValueAsString(entry));
            } catch (Exception e) {
                LOG.error("Error processing the entry with the id: "+entry.getId(),e);
            }
        }
        return jsonList;
    }
}
