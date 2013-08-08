package org.apache.streams.messaging.aggregation;


import org.apache.camel.Exchange;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.streams.cassandra.model.CassandraActivityStreamsEntry;
import org.apache.streams.cassandra.repository.impl.CassandraActivityStreamsRepository;
import org.apache.streams.messaging.service.impl.CassandraActivityService;
import org.apache.streams.osgi.components.activitysubscriber.ActivityStreamsSubscriber;
import org.apache.streams.osgi.components.activitysubscriber.ActivityStreamsSubscriberWarehouse;
import org.apache.streams.osgi.components.activitysubscriber.ActivityStreamsSubscriptionFilter;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.scheduling.annotation.Scheduled;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ActivityAggregator {

    private ActivityStreamsSubscriberWarehouse activityStreamsSubscriberWarehouse;
    private CassandraActivityService activityService;
    private static final transient Log LOG = LogFactory.getLog(ActivityAggregator.class);

    public void setActivityStreamsSubscriberWarehouse(ActivityStreamsSubscriberWarehouse activityStreamsSubscriberWarehouse) {
        this.activityStreamsSubscriberWarehouse = activityStreamsSubscriberWarehouse;
    }

    public void setActivityService(CassandraActivityService activityService) {
        this.activityService = activityService;
    }

    @Scheduled(fixedRate=30000)
    public void distributeToSubscribers() {
        for (ActivityStreamsSubscriber subscriber : activityStreamsSubscriberWarehouse.getAllSubscribers()) {
            Set<String> activities = new HashSet<String>();
            for (ActivityStreamsSubscriptionFilter filter: subscriber.getActivityStreamsSubscriberConfiguration().getActivityStreamsSubscriptionFilters()){
                //send the query of each filter to the service to receive the activities of that filter
                activities.addAll(activityService.getActivitiesForQuery(filter.getQuery()));
            }
            subscriber.receive(new ArrayList<String>(activities));
        }
    }
}
