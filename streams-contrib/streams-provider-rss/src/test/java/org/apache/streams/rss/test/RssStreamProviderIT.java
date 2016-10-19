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

package org.apache.streams.rss.test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.commons.lang.StringUtils;
import org.apache.streams.jackson.StreamsJacksonMapper;
import org.apache.streams.pojo.json.Activity;
import org.apache.streams.rss.FeedDetails;
import org.apache.streams.rss.RssStreamConfiguration;
import org.apache.streams.rss.provider.RssStreamProvider;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.List;

import static org.hamcrest.number.OrderingComparison.greaterThan;

/**
 * Created by sblackmon on 2/5/14.
 */
public class RssStreamProviderIT {

    private final static Logger LOGGER = LoggerFactory.getLogger(RssStreamProviderIT.class);

    private ObjectMapper mapper = StreamsJacksonMapper.getInstance();

    @Test
    public void testRssStreamProvider() throws Exception {

        String configfile = "./target/test-classes/RssStreamProviderIT.conf";
        String outfile = "./target/test-classes/RssStreamProviderIT.stdout.txt";

        InputStream is = RssStreamProviderIT.class.getResourceAsStream("/top100.txt");
        InputStreamReader isr = new InputStreamReader(is);
        BufferedReader br = new BufferedReader(isr);

        RssStreamConfiguration configuration = new RssStreamConfiguration();
        List<FeedDetails> feedArray = Lists.newArrayList();
        try {
            while (br.ready()) {
                String line = br.readLine();
                if(!StringUtils.isEmpty(line))
                {
                    feedArray.add(new FeedDetails().withUrl(line).withPollIntervalMillis(5000l));
                }
            }
            configuration.setFeeds(feedArray);
        } catch( Exception e ) {
            System.out.println(e);
            e.printStackTrace();
            Assert.fail();
        }

        Assert.assertThat(configuration.getFeeds().size(), greaterThan(70));

        OutputStream os = new FileOutputStream(configfile);
        OutputStreamWriter osw = new OutputStreamWriter(os);
        BufferedWriter bw = new BufferedWriter(osw);

        // write conf
        ObjectNode feedsNode = mapper.convertValue(configuration, ObjectNode.class);
        JsonNode configNode = mapper.createObjectNode().set("rss", feedsNode);

        bw.write(mapper.writeValueAsString(configNode));
        bw.flush();
        bw.close();

        File config = new File(configfile);
        assert (config.exists());
        assert (config.canRead());
        assert (config.isFile());

        RssStreamProvider.main(Lists.newArrayList(configfile, outfile).toArray(new String[2]));

        File out = new File(outfile);
        assert (out.exists());
        assert (out.canRead());
        assert (out.isFile());

        FileReader outReader = new FileReader(out);
        LineNumberReader outCounter = new LineNumberReader(outReader);

        while(outCounter.readLine() != null) {}

        assert (outCounter.getLineNumber() >= 200);

    }
}