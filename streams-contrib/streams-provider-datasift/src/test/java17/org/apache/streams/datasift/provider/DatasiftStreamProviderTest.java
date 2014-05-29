package org.apache.streams.datasift.provider;

import com.datasift.client.DataSiftClient;
import com.datasift.client.stream.StreamEventListener;
import com.datasift.client.stream.StreamingData;
import com.google.common.collect.Lists;
import org.apache.streams.datasift.DatasiftConfiguration;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.times;

/**
 * Requires Java version 1.7!
 */
public class DatasiftStreamProviderTest {

    private static final String HASH1 = "fake1";
    private static final String HASH2 = "fake2";
    private static final String HASH3 = "fake3";

    @Test
    public void startStreamForHash() {
        DatasiftStreamProvider.DeleteHandler handler = new DatasiftStreamProvider.DeleteHandler();
        final List<DataSiftClient> clientList = Lists.newLinkedList();
        DatasiftStreamProvider provider = createStubbedProvider(clientList, handler);
        provider.prepare(null);
        provider.startStreamForHash(HASH1);
        assertEquals(1, clientList.size());
        provider.startStreamForHash(HASH2);
        assertEquals(2, clientList.size());
        provider.startStreamForHash(HASH3);
    }

    @Test
    public void testStartStream() {
        DatasiftStreamProvider.DeleteHandler handler = new DatasiftStreamProvider.DeleteHandler();
        final List<DataSiftClient> clientList = Lists.newLinkedList();
        DatasiftStreamProvider provider = createStubbedProvider(clientList, handler);
        provider.prepare(null);
        provider.startStream();
        assertEquals(3, clientList.size());
    }

    @Test
    public void testShutDownStream() {
        DatasiftStreamProvider.DeleteHandler handler = new DatasiftStreamProvider.DeleteHandler();
        final List<DataSiftClient> clientList = Lists.newLinkedList();
        DatasiftStreamProvider provider = createStubbedProvider(clientList, handler);
        provider.prepare(null);
        provider.startStream();
        assertEquals(3, clientList.size());
        int shutDownCount = 0;
        DataSiftClient client = clientList.get(0);
        provider.shutDownStream(HASH1);
        Mockito.verify(client, times(1)).shutdown();
        client = clientList.get(1);
        Mockito.verify(client, times(0)).shutdown();
        client = clientList.get(2);
        Mockito.verify(client, times(0)).shutdown();

        provider.shutDownStream(HASH3);
        Mockito.verify(client, times(1)).shutdown();
        client = clientList.get(1);
        Mockito.verify(client, times(0)).shutdown();
        client = clientList.get(2);
        Mockito.verify(client, times(1)).shutdown();
    }

    @Test
    public void testStartAlreadyInprogressStream() {
        DatasiftStreamProvider.DeleteHandler handler = new DatasiftStreamProvider.DeleteHandler();
        final List<DataSiftClient> clientList = Lists.newLinkedList();
        DatasiftStreamProvider provider = createStubbedProvider(clientList, handler);
        provider.prepare(null);
        provider.startStream();
        assertEquals(3, clientList.size());
        int shutDownCount = 0;
        DataSiftClient client = clientList.get(0);
        provider.startStreamForHash(HASH1);
        assertEquals(4, clientList.size());
        Mockito.verify(client, times(1)).shutdown();
        client = clientList.get(1);
        Mockito.verify(client, times(0)).shutdown();
        client = clientList.get(2);
        Mockito.verify(client, times(0)).shutdown();
        client = clientList.get(3);
        Mockito.verify(client, times(0)).shutdown();
    }




    private DatasiftStreamProvider createStubbedProvider(final List<DataSiftClient> clientList, StreamEventListener listener) {
        DatasiftStreamProvider provider = new DatasiftStreamProvider(listener, getTestConfiguration()) {
            @Override
            protected DataSiftClient getNewClient(String userName, String apiKey) {
                DataSiftClient client = Mockito.mock(DataSiftClient.class);
                StreamingData mockData = Mockito.mock(StreamingData.class);
                Mockito.when(client.liveStream()).thenReturn(mockData);
                clientList.add(client);
                return client;
            }
        };
        return provider;
    }

    private DatasiftConfiguration getTestConfiguration() {
        DatasiftConfiguration config = new DatasiftConfiguration();
        config.setUserName("fakeName");
        config.setApiKey("fakeApiKey");
        List<String> streamHashes = Lists.newLinkedList();
        streamHashes.add(HASH1);
        streamHashes.add(HASH2);
        streamHashes.add(HASH3);
        config.setStreamHash(streamHashes);
        return config;
    }




}
