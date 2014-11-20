package org.apache.streams.dropwizard;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import com.typesafe.config.ConfigRenderOptions;
import org.apache.streams.config.StreamsConfiguration;
import org.apache.streams.config.StreamsConfigurator;

import java.io.IOException;

/**
 * Created by sblackmon on 11/18/14.
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
