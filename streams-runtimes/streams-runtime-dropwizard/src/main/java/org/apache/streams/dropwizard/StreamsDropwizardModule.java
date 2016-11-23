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
import org.apache.streams.config.StreamsConfigurator;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;

/**
 * This class exists because dropwizard-guice requires at least
 * one module to run.
 *
 * <p/>
 * Do not expect @Inject StreamsConfiguration to work at the moment.
 */
public class StreamsDropwizardModule extends AbstractModule {

  @Override
  protected void configure() {
    requestStaticInjection(StreamsConfiguration.class);
  }

  @Provides
  @Singleton
  public StreamsConfiguration providesStreamsConfiguration() {
    return StreamsConfigurator.detectConfiguration();
  }

}
