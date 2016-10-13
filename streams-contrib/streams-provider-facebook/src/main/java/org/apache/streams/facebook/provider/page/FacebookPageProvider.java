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
package org.apache.streams.facebook.provider.page;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import com.google.common.util.concurrent.Uninterruptibles;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import com.typesafe.config.ConfigParseOptions;
import org.apache.streams.config.ComponentConfigurator;
import org.apache.streams.config.StreamsConfiguration;
import org.apache.streams.config.StreamsConfigurator;
import org.apache.streams.core.StreamsDatum;
import org.apache.streams.facebook.FacebookConfiguration;
import org.apache.streams.facebook.provider.FacebookDataCollector;
import org.apache.streams.facebook.provider.FacebookProvider;
import org.apache.streams.jackson.StreamsJacksonMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.Iterator;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * Streams Provider which collects Page Profiles in the ID List contained in the
 * FacebookConfiguration object
 *
 * To use from command line:
 *
 *  Supply (at least) the following required configuration in application.conf:
 *
 *  facebook.oauth.appId
 *  facebook.oauth.appSecret
 *  facebook.oauth.userAccessToken
 *
 *  Launch using:
 *
 *  mvn exec:java -Dexec.mainClass=org.apache.streams.facebook.provider.page.FacebookPageProvider -Dexec.args="application.conf pages.json"

 */
public class FacebookPageProvider extends FacebookProvider {

    public static final String STREAMS_ID = "FacebookPageProvider";

    private static final Logger LOGGER = LoggerFactory.getLogger(FacebookPageProvider.class);

    private static ObjectMapper MAPPER = StreamsJacksonMapper.getInstance();

    public FacebookPageProvider(FacebookConfiguration facebookConfiguration) {
        super(facebookConfiguration);
    }

    @VisibleForTesting
    BlockingQueue<StreamsDatum> getQueue() {
        return super.datums;
    }

    @Override
    protected FacebookDataCollector getDataCollector() {
        return new FacebookPageDataCollector(super.datums, super.configuration);
    }

    public static void main(String[] args) throws Exception {

        Preconditions.checkArgument(args.length >= 2);

        String configfile = args[0];
        String outfile = args[1];

        Config reference = ConfigFactory.load();
        File conf_file = new File(configfile);
        assert(conf_file.exists());
        Config conf = ConfigFactory.parseFileAnySyntax(conf_file, ConfigParseOptions.defaults().setAllowMissing(false));

        Config typesafe  = conf.withFallback(reference).resolve();

        StreamsConfiguration streamsConfiguration = StreamsConfigurator.detectConfiguration(typesafe);
        FacebookConfiguration config = new ComponentConfigurator<>(FacebookConfiguration.class).detectConfiguration(typesafe, "facebook");
        FacebookPageProvider provider = new FacebookPageProvider(config);

        PrintStream outStream = new PrintStream(new BufferedOutputStream(new FileOutputStream(outfile)));
        provider.prepare(config);
        provider.startStream();
        do {
            Uninterruptibles.sleepUninterruptibly(streamsConfiguration.getBatchFrequencyMs(), TimeUnit.MILLISECONDS);
            Iterator<StreamsDatum> iterator = provider.readCurrent().iterator();
            while(iterator.hasNext()) {
                StreamsDatum datum = iterator.next();
                String json;
                try {
                    json = MAPPER.writeValueAsString(datum.getDocument());
                    outStream.println(json);
                } catch (JsonProcessingException e) {
                    System.err.println(e.getMessage());
                }
            }
        } while( provider.isRunning());
        provider.cleanUp();
        outStream.flush();
    }
}
