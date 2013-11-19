package org.apache.streams.components.service;

import org.apache.streams.components.service.impl.ActivityRepositoryServiceImpl;
import org.apache.streams.persistence.model.ActivityStreamsEntry;
import org.apache.streams.persistence.model.mongo.MongoActivityStreamsEntry;
import org.apache.streams.persistence.repository.ActivityStreamsRepository;
import org.junit.Before;
import org.junit.Test;

import java.util.*;

import static org.easymock.EasyMock.*;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.junit.Assert.assertThat;

public class StreamsActivityRepositoryServiceTest {

    private StreamsActivityRepositoryService activityRepositoryService;
    private ActivityStreamsRepository activityStreamsRepository;

    @Before
    public void setup() {
        activityStreamsRepository = createMock(ActivityStreamsRepository.class);

        activityRepositoryService = new ActivityRepositoryServiceImpl(activityStreamsRepository, MongoActivityStreamsEntry.class);
    }

    @Test
    public void receiveActivityTest() throws Exception {
        String validActivity = "{\n" +
                "            \"id\": \"id\",\n" +
                "            \"verb\": \"verb\",\n" +
                "            \"provider\": {\n" +
                "            \"displayName\":\"danny\",\n" +
                "            \"url\": \"http.example.com:8888\"\n" +
                "            },\n" +
                "            \"actor\": {\n" +
                "            \"id\": \"actorid\",\n" +
                "            \"objectType\": \"actorobject\",\n" +
                "            \"displayName\": \"actorname\",\n" +
                "            \"url\": \"www.actorexampleurl.com\",\n" +
                "            \"image\": {\"url\":\"www.actorexampleurl.com\"}\n" +
                "            },\n" +
                "            \"target\": {\n" +
                "            \"id\": \"targetid\",\n" +
                "            \"displayName\": \"targetname\",\n" +
                "            \"url\": \"www.targeturl.com\"\n" +
                "            },\n" +
                "            \"object\": {\n" +
                "            \"id\": \"objectid\",\n" +
                "            \"displayName\": \"objectname\",\n" +
                "            \"objectType\": \"object\",\n" +
                "            \"url\": \"www.objecturl.org\"\n" +
                "            }\n" +
                "            }";

        activityStreamsRepository.save(isA(ActivityStreamsEntry.class));
        expectLastCall();
        replay(activityStreamsRepository);

        activityRepositoryService.receiveActivity(validActivity);
        verify(activityStreamsRepository);
    }

    @Test
    public void getActivitiesForFilters(){
        ActivityStreamsEntry entry = new MongoActivityStreamsEntry();
        List<ActivityStreamsEntry> activityList = Arrays.asList(entry);
        Set<String> filters = new HashSet<String>();
        Date lastUpdated = new Date(1234);

        expect(activityStreamsRepository.getActivitiesForFilters(filters, lastUpdated)).andReturn(activityList);
        replay(activityStreamsRepository);

        List<ActivityStreamsEntry> returned = activityRepositoryService.getActivitiesForFilters(filters, lastUpdated);

        assertThat(activityList, is(sameInstance(returned)));
    }
}
