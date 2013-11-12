package org.apache.streams.components.service;

import org.apache.streams.components.activitysubscriber.ActivityStreamsSubscriberWarehouse;
import org.apache.streams.components.service.impl.StreamsActivityReceivingServiceImpl;
import org.junit.Before;
import org.junit.Test;

import static org.easymock.EasyMock.*;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

public class StreamsActivityReceivingServiceTest {
    private StreamsActivityReceivingService acitivityReceivingService;
    private ActivityStreamsSubscriberWarehouse activityStreamsSubscriberWarehouse;

    @Before
    public void setup(){
        activityStreamsSubscriberWarehouse = createMock(ActivityStreamsSubscriberWarehouse.class);

        acitivityReceivingService = new StreamsActivityReceivingServiceImpl(activityStreamsSubscriberWarehouse);
    }

    @Test
    public void getActivityTest() throws Exception{
        String inRoute = "my inroute";
        String stream = "hey look, it's a stream";

        expect(activityStreamsSubscriberWarehouse.getStream(inRoute)).andReturn(stream);
        replay(activityStreamsSubscriberWarehouse);

        assertThat(stream, is(equalTo(acitivityReceivingService.getActivity(inRoute))));
    }
}
