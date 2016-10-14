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

package com.google.gplus.provider;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.services.plus.Plus;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.util.concurrent.Uninterruptibles;
import com.google.gson.Gson;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import com.typesafe.config.ConfigParseOptions;
import org.apache.streams.config.ComponentConfigurator;
import org.apache.streams.config.StreamsConfiguration;
import org.apache.streams.config.StreamsConfigurator;
import org.apache.streams.core.StreamsDatum;
import org.apache.streams.google.gplus.GPlusConfiguration;
import org.apache.streams.google.gplus.configuration.UserInfo;
import org.apache.streams.jackson.StreamsJacksonMapper;
import org.apache.streams.util.api.requests.backoff.BackOffStrategy;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.Iterator;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 *
 */
public class GPlusUserDataProvider extends AbstractGPlusProvider{

    public final static String STREAMS_ID = "GPlusUserDataProvider";

    public GPlusUserDataProvider() {
        super();
    }

    public GPlusUserDataProvider(GPlusConfiguration config) {
        super(config);
    }

    @Override
    public String getId() {
        return STREAMS_ID;
    }

    @Override
    protected Runnable getDataCollector(BackOffStrategy strategy, BlockingQueue<StreamsDatum> queue, Plus plus, UserInfo userInfo) {
        return new GPlusUserDataCollector(plus, strategy, queue, userInfo);
    }

    public static void main(String[] args) throws Exception {

        Preconditions.checkArgument(args.length >= 2);

        String configfile = args[0];
        String outfile = args[1];

        Config reference = ConfigFactory.load();
        File conf_file = new File(configfile);
        assert(conf_file.exists());
        Config testResourceConfig = ConfigFactory.parseFileAnySyntax(conf_file, ConfigParseOptions.defaults().setAllowMissing(false));

        Config typesafe  = testResourceConfig.withFallback(reference).resolve();

        StreamsConfiguration streamsConfiguration = StreamsConfigurator.detectConfiguration(typesafe);
        GPlusConfiguration config = new ComponentConfigurator<>(GPlusConfiguration.class).detectConfiguration(typesafe, "gplus");
        GPlusUserDataProvider provider = new GPlusUserDataProvider(config);

        Gson gson = new Gson();

        PrintStream outStream = new PrintStream(new BufferedOutputStream(new FileOutputStream(outfile)));
        provider.prepare(config);
        provider.startStream();
        do {
            Uninterruptibles.sleepUninterruptibly(streamsConfiguration.getBatchFrequencyMs(), TimeUnit.MILLISECONDS);
            Iterator<StreamsDatum> iterator = provider.readCurrent().iterator();
            while(iterator.hasNext()) {
                StreamsDatum datum = iterator.next();
                String json;
                if (datum.getDocument() instanceof String)
                    json = (String) datum.getDocument();
                else
                    json = gson.toJson(datum.getDocument());
                outStream.println(json);
            }
        } while( provider.isRunning());
        provider.cleanUp();
        outStream.flush();
    }
}
