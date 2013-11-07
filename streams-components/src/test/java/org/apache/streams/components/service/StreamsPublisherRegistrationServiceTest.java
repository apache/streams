package org.apache.streams.components.service;

import org.apache.streams.components.service.impl.CassandraPublisherService;
import org.apache.streams.components.service.impl.StreamsPublisherRegistrationServiceImpl;
import org.apache.streams.persistence.model.ActivityStreamsPublisher;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.easymock.EasyMock.*;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

public class StreamsPublisherRegistrationServiceTest {
    private StreamsPublisherRegistrationService publisherRegistrationService;
    private StreamsPublisherRepositoryService publisherRepositoryService;

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Before
    public void setup(){
        publisherRepositoryService = createMock(CassandraPublisherService.class);
        publisherRegistrationService = new StreamsPublisherRegistrationServiceImpl(publisherRepositoryService);
    }

    @Test
    public void registerTest_SrcNull() throws Exception {
        String publisherJson = "{}";

        thrown.expect(Exception.class);
        thrown.expectMessage("configuration src is null");

        publisherRegistrationService.register(publisherJson);
    }

    @Test
    public void registerTest_SrcValid_inDB() throws Exception{
        String publisherJson = "{\"src\":\"this is my src!\"}";
        String inRoute = "this is returned inRoute";
        ActivityStreamsPublisher publisher= createMock(ActivityStreamsPublisher.class);

        expect(publisherRepositoryService.getActivityStreamsPublisherBySrc("this is my src!")).andReturn(publisher);
        expect(publisher.getInRoute()).andReturn(inRoute);
        replay(publisherRepositoryService,publisher);

        String returned = publisherRegistrationService.register(publisherJson);

        assertThat(returned, is(equalTo(inRoute)));
    }

    @Test
    public void registerTest_SrcValid_notInDB() throws Exception{
        String publisherJson = "{\"src\":\"this is my src!\"}";
        String inRoute = "this is returned inRoute";

        expect(publisherRepositoryService.getActivityStreamsPublisherBySrc("this is my src!")).andReturn(null);
        publisherRepositoryService.savePublisher(isA(ActivityStreamsPublisher.class));
        expectLastCall();
        replay(publisherRepositoryService);

        String returned = publisherRegistrationService.register(publisherJson);

        assertThat(returned, is(instanceOf(String.class)));
        verify(publisherRepositoryService);
    }
}
