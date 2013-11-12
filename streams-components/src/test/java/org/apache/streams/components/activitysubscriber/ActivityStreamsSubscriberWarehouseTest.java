package org.apache.streams.components.activitysubscriber;

import org.apache.streams.components.activitysubscriber.impl.ActivityStreamsSubscriberWarehouseImpl;
import org.apache.streams.components.service.StreamsActivityRepositoryService;
import org.apache.streams.persistence.model.ActivityStreamsEntry;
import org.apache.streams.persistence.model.ActivityStreamsSubscription;
import org.apache.streams.persistence.model.cassandra.CassandraActivityStreamsEntry;
import org.apache.streams.persistence.model.cassandra.CassandraSubscription;
import org.junit.Before;
import org.junit.Test;

import java.util.*;

import static org.easymock.EasyMock.*;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

public class ActivityStreamsSubscriberWarehouseTest {
    private ActivityStreamsSubscriberWarehouse warehouse;
    private StreamsActivityRepositoryService activityRepositoryService;

    @Before
    public void setup() {
        activityRepositoryService = createMock(StreamsActivityRepositoryService.class);
        warehouse = new ActivityStreamsSubscriberWarehouseImpl(activityRepositoryService);
    }

    @Test
    public void registerTest() throws Exception{
        ActivityStreamsSubscription subscription = new CassandraSubscription();
        subscription.setInRoute("myFakeInroute");

        warehouse.register(subscription);

        assertNotNull(warehouse.getSubscriber("myFakeInroute"));
        assertThat(warehouse.getStream("myFakeInroute"), is(equalTo("[]")));
        assertThat(warehouse.getStream("notInTheWarehouse"),is(equalTo("Registration Needed")));
    }

    @Test
    public void updateSubscriberTest() throws Exception {
        ActivityStreamsSubscription subscription = new CassandraSubscription();
        String activityJson = "[{\"id\":null,\"tags\":null,\"published\":1235,\"verb\":\"this is newer\",\"actor\":null,\"object\":null,\"provider\":null,\"target\":null}, {\"id\":null,\"tags\":null,\"published\":1234,\"verb\":\"this is older\",\"actor\":null,\"object\":null,\"provider\":null,\"target\":null}]";
        Set<String> filters = new HashSet<String>(Arrays.asList("filters"));
        subscription.setInRoute("myFakeInroute");
        subscription.setFilters(filters);

        ActivityStreamsEntry entry1 = new CassandraActivityStreamsEntry();
        ActivityStreamsEntry entry2 = new CassandraActivityStreamsEntry();

        entry1.setPublished(new Date(1234L));
        entry2.setPublished(new Date(1235L));

        entry1.setVerb("this is older");
        entry2.setVerb("this is newer");

        List<ActivityStreamsEntry> entryList = Arrays.asList(entry1, entry2);

        expect(activityRepositoryService.getActivitiesForProviders(eq(filters),isA(Date.class))).andReturn(entryList);
        replay(activityRepositoryService);

        warehouse.register(subscription);
        warehouse.updateSubscriber(subscription);

        assert(warehouse.getSubscriber("myFakeInroute").getLastUpdated().getTime() != 0L);
        assertThat(warehouse.getStream("myFakeInroute"),is(equalTo(activityJson)));
    }


}
