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

package org.apache.streams.gplus.provider;

import org.apache.streams.config.ComponentConfigurator;
import org.apache.streams.config.StreamsConfiguration;
import org.apache.streams.config.StreamsConfigurator;
import org.apache.streams.core.StreamsDatum;
import org.apache.streams.google.gplus.GPlusConfiguration;
import org.apache.streams.google.gplus.configuration.UserInfo;
import org.apache.streams.util.api.requests.backoff.BackOffStrategy;

import com.google.api.services.plus.Plus;
import com.google.common.base.Preconditions;
import com.google.common.util.concurrent.Uninterruptibles;
import com.google.gson.Gson;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import com.typesafe.config.ConfigParseOptions;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 *  Retrieve recent activity from a list of accounts.
 *
 *  <p/>
 *  To use from command line:
 *
 *  <p/>
 *  Supply (at least) the following required configuration in application.conf:
 *
 *  <p/>
 *  gplus.oauth.pathToP12KeyFile
 *  gplus.oauth.serviceAccountEmailAddress
 *  gplus.apiKey
 *  gplus.googlePlusUsers
 *
 *  <p/>
 *  Launch using:
 *
 *  <p/>
 *  mvn exec:java -Dexec.mainClass=org.apache.streams.gplus.provider.GPlusUserActivityProvider -Dexec.args="application.conf activity.json"
 */
public class GPlusUserActivityProvider extends AbstractGPlusProvider {

  private static final String STREAMS_ID = "GPlusUserActivityProvider";

  public GPlusUserActivityProvider() {
    super();
  }

  public GPlusUserActivityProvider(GPlusConfiguration config) {
    super(config);
  }

  @Override
  public String getId() {
    return STREAMS_ID;
  }

  @Override
  protected Runnable getDataCollector(BackOffStrategy strategy, BlockingQueue<StreamsDatum> queue, Plus plus, UserInfo userInfo) {
    return new GPlusUserActivityCollector(plus, queue, strategy, userInfo);
  }

  /**
   * Retrieve recent activity from a list of accounts.
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
    Config testResourceConfig = ConfigFactory.parseFileAnySyntax(file, ConfigParseOptions.defaults().setAllowMissing(false));

    Config typesafe  = testResourceConfig.withFallback(reference).resolve();

    StreamsConfiguration streamsConfiguration = StreamsConfigurator.detectConfiguration(typesafe);
    GPlusConfiguration config = new ComponentConfigurator<>(GPlusConfiguration.class).detectConfiguration(typesafe, "gplus");
    GPlusUserActivityProvider provider = new GPlusUserActivityProvider(config);

    Gson gson = new Gson();

    PrintStream outStream = new PrintStream(new BufferedOutputStream(new FileOutputStream(outfile)));
    provider.prepare(config);
    provider.startStream();
    do {
      Uninterruptibles.sleepUninterruptibly(streamsConfiguration.getBatchFrequencyMs(), TimeUnit.MILLISECONDS);
      for (StreamsDatum datum : provider.readCurrent()) {
        String json;
        if (datum.getDocument() instanceof String) {
          json = (String) datum.getDocument();
        } else {
          json = gson.toJson(datum.getDocument());
        }
        outStream.println(json);
      }
    }
    while ( provider.isRunning());
    provider.cleanUp();
    outStream.flush();
  }
}
