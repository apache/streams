package org.apache.streams.components.service.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.streams.components.activitysubscriber.ActivityStreamsSubscriberWarehouse;
import org.apache.streams.components.service.StreamsSubscriptionRepositoryService;
import org.apache.streams.components.service.StreamsTagsService;
import org.apache.streams.persistence.model.ActivityStreamsSubscription;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Map;

@Component
public class StreamsTagsServiceImpl implements StreamsTagsService {
    private static final Log LOG = LogFactory.getLog(StreamsTagsServiceImpl.class);

    private StreamsSubscriptionRepositoryService repositoryService;
    private ActivityStreamsSubscriberWarehouse subscriberWarehouse;
    private ObjectMapper mapper;

    @Autowired
    public StreamsTagsServiceImpl(StreamsSubscriptionRepositoryService repositoryService, ActivityStreamsSubscriberWarehouse subscriberWarehouse) {
        this.repositoryService = repositoryService;
        this.subscriberWarehouse = subscriberWarehouse;
        this.mapper = new ObjectMapper();
        mapper.configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    @Override
    public String getTags(String subscriberId) throws Exception{
        ActivityStreamsSubscription subscription = repositoryService.getSubscriptionByInRoute(subscriberId);
        return mapper.writeValueAsString(subscription.getTags());
    }

    @Override
    public String updateTags(String subscriberId, String tagsJson) throws Exception {
        LOG.info("updating tags for " + subscriberId);

        Map<String, List> updateTags = (Map<String, List>) mapper.readValue(tagsJson, Map.class);
        repositoryService.updateTags(subscriberId, new HashSet<String>(updateTags.get("add")), new HashSet<String>(updateTags.get("remove")));
        subscriberWarehouse.updateSubscriber(repositoryService.getSubscriptionByInRoute(subscriberId));

        return "Tags Updated Successfully!";
    }
}
