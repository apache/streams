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

package org.apache.streams.dropwizard;

import org.apache.streams.config.StreamsConfiguration;
import org.apache.streams.core.StreamBuilder;
import org.apache.streams.core.StreamsProvider;
import org.apache.streams.jackson.StreamsJacksonMapper;

import com.codahale.metrics.MetricRegistry;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.guava.GuavaModule;
import com.fasterxml.jackson.module.afterburner.AfterburnerModule;
import com.hubspot.dropwizard.guice.GuiceBundle;
import io.dropwizard.Application;
import io.dropwizard.jackson.GuavaExtrasModule;
import io.dropwizard.metrics.MetricsFactory;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * Entry point to a dropwizard streams application
 *
 * <p/>
 * It will start up a stream in the local runtime, as well as bind any
 * StreamsProvider on the classpath with a @Resource annotation.
 */
public class StreamsApplication extends Application<StreamsDropwizardConfiguration> {

  private static final Logger LOGGER = LoggerFactory
      .getLogger(StreamsApplication.class);

  protected static ObjectMapper mapper = StreamsJacksonMapper.getInstance();

  protected StreamBuilder builder;

  private static StreamsConfiguration streamsConfiguration;

  // ConcurrentHashSet is preferable, but it's only in guava 15+
  // spark 1.5.0 uses guava 14 so for the moment this is the workaround
  // Set<StreamsProvider> resourceProviders = Sets.newConcurrentHashSet();
  private Set<StreamsProvider> resourceProviders = Collections.newSetFromMap(new ConcurrentHashMap<StreamsProvider, Boolean>());

  private Executor executor = Executors.newSingleThreadExecutor();

  static {
    mapper.registerModule(new AfterburnerModule());
    mapper.registerModule(new GuavaModule());
    mapper.registerModule(new GuavaExtrasModule());
  }

  @Override
  public void initialize(Bootstrap<StreamsDropwizardConfiguration> bootstrap) {

    LOGGER.info(getClass().getPackage().getName());

    GuiceBundle<StreamsDropwizardConfiguration> guiceBundle =
        GuiceBundle.<StreamsDropwizardConfiguration>newBuilder()
            .addModule(new StreamsDropwizardModule())
            .setConfigClass(StreamsDropwizardConfiguration.class)
            // override and add more packages to pick up custom Resources
            .enableAutoConfig(getClass().getPackage().getName())
            .build();
    bootstrap.addBundle(guiceBundle);

  }

  @Override
  public void run(StreamsDropwizardConfiguration streamsDropwizardConfiguration, Environment environment) throws Exception {

    executor = Executors.newSingleThreadExecutor();

    for ( Class<?> resourceProviderClass : environment.jersey().getResourceConfig().getRootResourceClasses() ) {
      StreamsProvider provider = (StreamsProvider)resourceProviderClass.newInstance();
      if ( StreamsProvider.class.isInstance(provider)) {
        resourceProviders.add(provider);
      }
    }

    MetricRegistry metrics = new MetricRegistry();
    MetricsFactory mfac = streamsDropwizardConfiguration.getMetricsFactory();
    mfac.configure(environment.lifecycle(), metrics);

    streamsConfiguration = mapper.convertValue(streamsDropwizardConfiguration, StreamsConfiguration.class);

    builder = setup(streamsConfiguration, resourceProviders);

    executor.execute(new StreamsDropwizardRunner(builder, streamsConfiguration));

    // wait for streams to start up
    Thread.sleep(10000);

    for (StreamsProvider resource : resourceProviders) {
      environment.jersey().register(resource);
      LOGGER.info("Added resource class: {}", resource);
    }

  }

  /**
   * setup StreamBuilder.
   * @param streamsConfiguration StreamsConfiguration
   * @param resourceProviders Set of StreamsProvider
   * @return StreamBuilder
   */
  public StreamBuilder setup(StreamsConfiguration streamsConfiguration, Set<StreamsProvider> resourceProviders) {

    StreamBuilder builder = new StreamDropwizardBuilder();

    List<String> providers = new ArrayList<>();
    for ( StreamsProvider provider: resourceProviders) {
      String providerId = provider.getClass().getSimpleName();
      builder.newPerpetualStream(providerId, provider);
      providers.add(providerId);
    }

    return builder;
  }

  private class StreamsDropwizardRunner implements Runnable {

    private StreamsConfiguration streamsConfiguration;

    private StreamBuilder builder;

    protected StreamsDropwizardRunner(StreamBuilder builder, StreamsConfiguration streamsConfiguration) {
      this.streamsConfiguration = streamsConfiguration;
      this.builder = builder;
    }

    @Override
    public void run() {

      builder.start();

    }
  }

  /**
   * Run from console:
   *
   * <p/>
   * java -jar uber.jar server ./configuration.yml
   *
   * @param args ["server", configuration.yml]
   * @throws Exception Exception
   */
  public static void main(String[] args) throws Exception {

    new StreamsApplication().run(args);

  }

}
