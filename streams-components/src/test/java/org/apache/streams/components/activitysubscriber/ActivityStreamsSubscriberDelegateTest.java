package org.apache.streams.components.activitysubscriber;

import org.apache.streams.components.activitysubscriber.impl.ActivityStreamsSubscriberDelegate;
import org.apache.streams.persistence.model.ActivityStreamsEntry;
import org.apache.streams.persistence.model.cassandra.CassandraActivityStreamsEntry;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Date;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class ActivityStreamsSubscriberDelegateTest {
    private ActivityStreamsSubscriberDelegate subscriber;

    @Before
    public void setup(){
        subscriber = new ActivityStreamsSubscriberDelegate();
    }

    @Test
    public void getStreamTestFullStream() throws Exception {
        ActivityStreamsEntry entry1 = new CassandraActivityStreamsEntry();
        entry1.setPublished(new Date(1234L));

        ActivityStreamsEntry entry2 = new CassandraActivityStreamsEntry();
        entry2.setPublished(new Date(1235L));

        String streamString = "[{\"id\":null,\"tags\":null,\"published\":1235,\"verb\":null,\"actor\":null,\"object\":null,\"provider\":null,\"target\":null}, {\"id\":null,\"tags\":null,\"published\":1234,\"verb\":null,\"actor\":null,\"object\":null,\"provider\":null,\"target\":null}]";

        subscriber.receive(Arrays.asList(entry1, entry2));
        String returned = subscriber.getStream();

        assertThat(returned,is(equalTo(streamString)));
    }

}
