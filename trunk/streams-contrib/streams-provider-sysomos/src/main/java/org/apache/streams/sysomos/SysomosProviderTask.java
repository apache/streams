package org.apache.streams.sysomos;

import com.sysomos.SysomosConfiguration;
import org.apache.commons.io.IOUtils;
import org.apache.streams.core.StreamsDatum;
import org.apache.streams.core.StreamsProvider;
import org.apache.streams.core.StreamsResultSet;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Queue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Wrapper for the Sysomos API.
 */
public class SysomosProviderTask implements Runnable {

    private final static Logger LOGGER = LoggerFactory.getLogger(SysomosProviderTask.class);

    private SysomosConfiguration config;

    private SysomosProvider provider;

    private SysomosClient client;

    private String heartbeatId;

    public SysomosProviderTask(SysomosProvider provider, String heartbeatId) {
        this.provider = provider;
        this.heartbeatId = heartbeatId;
    }

    @Override
    public void run() {

        client = new SysomosClient(provider.getConfig().getApiKey());

    }
}
