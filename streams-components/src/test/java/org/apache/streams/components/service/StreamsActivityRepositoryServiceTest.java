package org.apache.streams.components.service;

import org.apache.streams.persistence.model.ActivityStreamsEntry;
import org.apache.streams.persistence.model.cassandra.CassandraActivityStreamsEntry;
import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Test;

import java.io.IOException;

public class StreamsActivityRepositoryServiceTest {

    @Test
    public void parseJsonTest() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        String activityJson = "{\n" +
                "     \"verb\": \"verb\",\n" +
                "     \"tags\": \"tags\",\n" +
                "     \"provider\": {\n" +
                "          \"url\": \"www.example.com\"\n" +
                "     },\n" +
                "     \"actor\": {\n" +
                "          \"id\": \"actorid\",\n" +
                "          \"objectType\": \"actorobject\",\n" +
                "          \"displayName\": \"actorname\",\n" +
                "          \"url\": \"www.actorexampleurl.com\"\n" +
                "     },\n" +
                "     \"target\": {\n" +
                "           \"id\": \"targetid\",\n" +
                "           \"displayName\": \"targetname\",\n" +
                "           \"url\": \"www.targeturl.com\"\n" +
                "     },\n" +
                "     \"object\": {\n" +
                "           \"id\": \"objectid\",\n" +
                "           \"displayName\": \"objectname\",\n" +
                "           \"objectType\": \"object\",\n" +
                "           \"url\": \"www.objecturl.org\"\n" +
                "       }\n" +
                " }";

        ActivityStreamsEntry a2 = mapper.readValue(activityJson,CassandraActivityStreamsEntry.class);

    }
}
