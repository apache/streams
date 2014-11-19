package org.apache.streams.dropwizard;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
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
        // anything you'd like to configure
    }

    @Provides
    public StreamsConfiguration providesStreamsConfiguration(StreamsDropwizardConfiguration configuration) {
        return StreamsConfigurator.detectConfiguration();
    }

//    private StreamsDropwizardConfiguration reconfigure(StreamsDropwizardConfiguration streamsConfiguration) {
//
//        // config from dropwizard
//        Config configDropwizard = null;
//        try {
//            configDropwizard = ConfigFactory.parseString(mapper.writeValueAsString(streamsConfiguration));
//        } catch (JsonProcessingException e) {
//            e.printStackTrace();
//            LOGGER.error("Invalid Configuration: " + streamsConfiguration);
//        }
//
//        Config combinedConfig = configTypesafe.withFallback(configDropwizard);
//        String combinedConfigJson = combinedConfig.root().render(ConfigRenderOptions.concise());
//
//        StreamsDropwizardConfiguration combinedDropwizardConfig = null;
//        try {
//            combinedDropwizardConfig = mapper.readValue(combinedConfigJson, StreamsDropwizardConfiguration.class);
//        } catch (IOException e) {
//            e.printStackTrace();
//            LOGGER.error("Invalid Configuration after merge: " + streamsConfiguration);
//        }
//
//        return  combinedDropwizardConfig;
//
//    }
}
