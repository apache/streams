/*
Licensed to the Apache Software Foundation (ASF) under one
or more contributor license agreements. See the NOTICE file
distributed with this work for additional information
regarding copyright ownership. The ASF licenses this file
to you under the Apache License, Version 2.0 (the
"License"); you may not use this file except in compliance *
http://www.apache.org/licenses/LICENSE-2.0 *
Unless required by applicable law or agreed to in writing,
software distributed under the License is distributed on an
"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
KIND, either express or implied. See the License for the
specific language governing permissions and limitations
under the License. */

package org.apache.streams.instagram.provider;

import org.apache.streams.config.ComponentConfigurator;
import org.apache.streams.config.StreamsConfiguration;
import org.apache.streams.config.StreamsConfigurator;
import org.apache.streams.core.StreamsDatum;
import org.apache.streams.instagram.config.InstagramLikersProviderConfiguration;
import org.apache.streams.jackson.StreamsJacksonMapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Preconditions;
import com.google.common.util.concurrent.Uninterruptibles;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import com.typesafe.config.ConfigParseOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.Iterator;
import java.util.concurrent.TimeUnit;

/**
 * Instagram {@link org.apache.streams.core.StreamsProvider} that provides likers of all items in a list of media.
 *
 * <p/>
 * Retrieve likers of all items in a list of media.
 *
 */
public class InstagramLikersProvider extends InstagramAbstractProvider {

  public static final String STREAMS_ID = "InstagramLikersProvider";

  private static final Logger LOGGER = LoggerFactory.getLogger(InstagramLikersProvider.class);

  private static ObjectMapper MAPPER = StreamsJacksonMapper.getInstance();

  InstagramLikersProviderConfiguration config;

  public InstagramLikersProvider(InstagramLikersProviderConfiguration config) {
    super(config);
    this.config = config;
  }

  @Override
  protected InstagramDataCollector getInstagramDataCollector() {
    return new InstagramLikersCollector(client, super.dataQueue, config);
  }

  /**
   * To use from command line:
   *
   * <p/>
   * Supply (at least) the following required configuration in application.conf:
   *
   * <p/>
   * instagram.clientKey
   * instagram.usersInfo.authorizedTokens
   * instagram.usersInfo.users
   *
   * <p/>
   * Launch using:
   *
   * <p/>
   * mvn exec:java \
   * -Dexec.mainClass=org.apache.streams.instagram.provider.recentmedia.InstagramLikersProvider \
   * -Dexec.args="application.conf likers.json.txt"
   *
   * @param args args
   * @throws Exception Exception
   */
  public static void main(String[] args) throws Exception {

    Preconditions.checkArgument(args.length >= 2);

    String configfile = args[0];
    String outfile = args[1];

    Config reference = ConfigFactory.load();
    File file = new File(configfile);
    assert (file.exists());

    Config conf = ConfigFactory.parseFileAnySyntax(file, ConfigParseOptions.defaults().setAllowMissing(false));
    StreamsConfigurator.addConfig(conf);

    StreamsConfiguration streamsConfiguration = StreamsConfigurator.detectConfiguration();
    InstagramLikersProviderConfiguration config = new ComponentConfigurator<>(InstagramLikersProviderConfiguration.class).detectConfiguration();
    InstagramLikersProvider provider = new InstagramLikersProvider(config);

    PrintStream outStream = new PrintStream(new BufferedOutputStream(new FileOutputStream(outfile)));
    provider.prepare(config);
    provider.startStream();
    do {
      Uninterruptibles.sleepUninterruptibly(streamsConfiguration.getBatchFrequencyMs(), TimeUnit.MILLISECONDS);
      Iterator<StreamsDatum> iterator = provider.readCurrent().iterator();
      while (iterator.hasNext()) {
        StreamsDatum datum = iterator.next();
        String json;
        try {
          json = MAPPER.writeValueAsString(datum.getDocument());
          outStream.println(json);
        } catch (JsonProcessingException ex) {
          System.err.println(ex.getMessage());
        }
      }
    }
    while ( provider.isRunning());
    provider.cleanUp();
    outStream.flush();
  }
}
