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
 * Created by steve on 11/22/14.
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
     * HttpClients mock.
     */
    private HttpClients httpClients;

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

    /**
     * Instance under tests.
     */
    private SimpleHTTPPostPersistWriter writer;

    @Before
    public void setUp() throws Exception
    {
        this.httpClients = PowerMockito.mock(HttpClients.class);
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
        configuration.setPort(new Long(PORT));
        configuration.setResourcePath("/");

        this.writer = new SimpleHTTPPostPersistWriter(configuration);

        this.writer.prepare(null);

        StreamsDatum testDatum = new StreamsDatum(mapper.readValue("{\"message\":\"ping\"}", ObjectNode.class));

        this.writer.write(testDatum);

        Mockito.verify(this.client).execute(any(HttpUriRequest.class));

        Mockito.verify(this.response).close();

    }
}
