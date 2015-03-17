package com.youtube.provider;

import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.Channel;
import com.google.api.services.youtube.model.ChannelListResponse;
import com.google.common.collect.Lists;
import com.google.common.collect.Queues;
import org.apache.streams.core.StreamsDatum;
import org.apache.streams.google.gplus.configuration.UserInfo;
import org.apache.streams.util.api.requests.backoff.BackOffStrategy;
import org.apache.streams.util.api.requests.backoff.impl.LinearTimeBackOffStrategy;
import org.apache.youtube.pojo.YoutubeConfiguration;
import org.junit.Test;

import java.util.List;
import java.util.concurrent.BlockingQueue;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by rebanks on 2/17/15.
 */
public class YoutubeChannelDataCollectorTest {

    private static final String ID = "12345";

    @Test
    public void testDataCollector() throws Exception {
        YouTube youTube = createMockYoutube();
        BlockingQueue<StreamsDatum> queue = Queues.newLinkedBlockingQueue();
        BackOffStrategy strategy = new LinearTimeBackOffStrategy(1);
        UserInfo userInfo = new UserInfo();
        userInfo.setUserId(ID);
        YoutubeConfiguration config = new YoutubeConfiguration();
        config.setApiKey(ID);
        YoutubeChannelDataCollector collector = new YoutubeChannelDataCollector(youTube, queue, strategy, userInfo, config);
        collector.run();
        assertEquals(1, queue.size());
        StreamsDatum datum = queue.take();
        assertNotNull(datum);
        String document = (String) datum.getDocument();
        assertNotNull(document);
    }

    private YouTube createMockYoutube() throws Exception {
        YouTube mockYouTube = mock(YouTube.class);
        YouTube.Channels channels = createMockChannels();
        when(mockYouTube.channels()).thenReturn(channels);
        return mockYouTube;
    }

    private YouTube.Channels createMockChannels() throws Exception {
        YouTube.Channels mockChannels = mock(YouTube.Channels.class);
        YouTube.Channels.List channelLists = createMockChannelsList();
        when(mockChannels.list(anyString())).thenReturn(channelLists);
        return mockChannels;
    }

    private YouTube.Channels.List createMockChannelsList() throws Exception {
        YouTube.Channels.List mockList = mock(YouTube.Channels.List.class);
        when(mockList.setId(anyString())).thenReturn(mockList);
        when(mockList.setKey(anyString())).thenReturn(mockList);
        ChannelListResponse response = createMockResponse();
        when(mockList.execute()).thenReturn(response);
        return mockList;
    }

    private ChannelListResponse createMockResponse() {
        ChannelListResponse response = new ChannelListResponse();
        List<Channel> channelList = Lists.newLinkedList();
        response.setItems(channelList);
        Channel channel = new Channel();
        channel.setId(ID);
        channelList.add(channel);
        return response;
    }

}
