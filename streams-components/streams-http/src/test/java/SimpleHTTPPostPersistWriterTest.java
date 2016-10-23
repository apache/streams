/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.streams.components.http.HttpPersistWriterConfiguration;
import org.apache.streams.components.http.persist.SimpleHTTPPostPersistWriter;
import org.apache.streams.core.StreamsDatum;
import org.apache.streams.jackson.StreamsJacksonMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import static org.mockito.Matchers.any;

/**
 * Test for
 * @see org.apache.streams.components.http.persist.SimpleHTTPPostPersistWriter
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({HttpClients.class, CloseableHttpResponse.class, CloseableHttpResponse.class})
public class SimpleHTTPPostPersistWriterTest {

    private ObjectMapper mapper = StreamsJacksonMapper.getInstance();

    /**
     * test port.
     */
    private static final int PORT = 18080;

    /**
     * test hosts.
     */
    private static final String HOSTNAME = "localhost";

    /**
     * test protocol.
     */
    private static final String PROTOCOL = "http";

    /**
     * CloseableHttpClient mock.
     */
    private CloseableHttpClient client;

    /**
     * CloseableHttpClient mock.
     */
    private CloseableHttpResponse response = Mockito.mock(CloseableHttpResponse.class);

    /**
     * Our output.
     */
    private ByteArrayOutputStream output;

    /**
     * Our input.
     */
    private ByteArrayInputStream input;

    @Before
    public void setUp() throws Exception
    {
        /*
      HttpClients mock.
     */
        this.client = PowerMockito.mock(CloseableHttpClient.class);

        PowerMockito.mockStatic(HttpClients.class);

        PowerMockito.when(HttpClients.createDefault())
                .thenReturn(client);

        PowerMockito.when(client.execute(any(HttpUriRequest.class)))
                .thenReturn(response);

        Mockito.when(response.getEntity()).thenReturn(null);
        Mockito.doNothing().when(response).close();

    }

    @Test
    public void testPersist() throws Exception
    {
        HttpPersistWriterConfiguration configuration = new HttpPersistWriterConfiguration();
        configuration.setProtocol(PROTOCOL);
        configuration.setHostname(HOSTNAME);
        configuration.setPort((long) PORT);
        configuration.setResourcePath("/");

        /*
      Instance under tests.
     */
        SimpleHTTPPostPersistWriter writer = new SimpleHTTPPostPersistWriter(configuration);

        writer.prepare(null);

        StreamsDatum testDatum = new StreamsDatum(mapper.readValue("{\"message\":\"ping\"}", ObjectNode.class));

        writer.write(testDatum);

        Mockito.verify(this.client).execute(any(HttpUriRequest.class));

        Mockito.verify(this.response).close();

    }
}
