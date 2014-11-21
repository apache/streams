package org.apache.streams.dropwizard.test;

import com.google.common.collect.Lists;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;

/**
 * Tests {@link: org.apache.streams.dropwizard.StreamsApplication}
 */
public class StreamsApplicationIT {

    @Before
    public void setupTest() throws Exception {
        String[] testArgs = Lists.newArrayList("server", "src/test/resources/configuration.yml").toArray(new String[2]);
        TestStreamsApplication.main(testArgs);
    }

    @Test
    public void testApplicationStarted() throws Exception {

        final URL url = new URL("http://localhost:8003/admin/ping");
        final String response = new BufferedReader(new InputStreamReader(url.openStream())).readLine();
        Assert.assertEquals("pong", response);
    }
}
