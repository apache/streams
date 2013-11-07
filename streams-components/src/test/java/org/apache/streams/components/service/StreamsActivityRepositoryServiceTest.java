package org.apache.streams.components.service;

import org.apache.streams.components.service.impl.CassandraActivityService;
import org.apache.streams.persistence.model.ActivityStreamsEntry;
import org.apache.streams.persistence.model.ActivityStreamsPublisher;
import org.apache.streams.persistence.model.cassandra.CassandraActivityStreamsEntry;
import org.apache.streams.persistence.repository.ActivityStreamsRepository;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.*;

import static org.easymock.EasyMock.*;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

public class StreamsActivityRepositoryServiceTest {
    private StreamsActivityRepositoryService activityRepositoryService;
    private ActivityStreamsRepository activityStreamsRepository;

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Before
    public void setup() {
        activityStreamsRepository = createMock(ActivityStreamsRepository.class);

        activityRepositoryService = new CassandraActivityService(activityStreamsRepository);
    }

    @Test
    public void receiveActivityTest_SrcEqual() throws Exception {
        ActivityStreamsPublisher activityStreamsPublisher = createMock(ActivityStreamsPublisher.class);
        String activityJson = "{\"provider\":{\"url\":\"myUrl\"}}";

        expect(activityStreamsPublisher.getSrc()).andReturn("myUrl");
        activityStreamsRepository.save(isA(ActivityStreamsEntry.class));
        expectLastCall();
        replay(activityStreamsRepository, activityStreamsPublisher);

        activityRepositoryService.receiveActivity(activityStreamsPublisher,activityJson);

        verify(activityStreamsRepository);

    }

    @Test
    public void receiveActivityTest_SrcNotEqual() throws Exception {
        ActivityStreamsPublisher activityStreamsPublisher = createMock(ActivityStreamsPublisher.class);
        String activityJson = "{\"provider\":{\"url\":\"actSrc\"}}";

        expect(activityStreamsPublisher.getSrc()).andReturn("pubSrc");
        expect(activityStreamsPublisher.getSrc()).andReturn("pubSrc");
        replay(activityStreamsPublisher);

        thrown.expect(Exception.class);
        thrown.expectMessage("The Publisher source: pubSrc and Activity Provider source: actSrc were not equal");

        activityRepositoryService.receiveActivity(activityStreamsPublisher,activityJson);
    }

    @Test
    public void getActivitiesForTagsTest_Valid(){
        Set<String> tags = new HashSet<String>(Arrays.asList("tags"));

        String entry1Json = "{\"id\":\"entry1\",\"tags\":null,\"published\":1234,\"verb\":null,\"actor\":null,\"object\":null,\"provider\":null,\"target\":null}";
        String entry2Json = "{\"id\":\"entry2\",\"tags\":null,\"published\":5678,\"verb\":null,\"actor\":null,\"object\":null,\"provider\":null,\"target\":null}";

        Date begin = new Date(0L);
        ActivityStreamsEntry entry1 = new CassandraActivityStreamsEntry();
        Date published1 = new Date(1234L);
        entry1.setPublished(published1);
        entry1.setId("entry1");

        ActivityStreamsEntry entry2 = new CassandraActivityStreamsEntry();
        Date published2 = new Date(5678L);
        entry2.setPublished(published2);
        entry2.setId("entry2");

        List<ActivityStreamsEntry> activities = new ArrayList<ActivityStreamsEntry>();
        activities.add(entry1);
        activities.add(entry2);

        expect(activityStreamsRepository.getActivitiesForTags(tags, begin)).andReturn(activities);
        replay(activityStreamsRepository);

        List<String> returned = activityRepositoryService.getActivitiesForTags(tags,begin);

        assertThat(returned.get(0),is(equalTo(entry2Json)));
        assertThat(returned.get(1),is(equalTo(entry1Json)));
    }
}
