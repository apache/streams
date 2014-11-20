package org.apache.streams.dropwizard;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.guava.GuavaModule;
import com.fasterxml.jackson.module.afterburner.AfterburnerModule;
import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.hubspot.dropwizard.guice.GuiceBundle;
import com.sun.jersey.api.core.ResourceConfig;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import com.typesafe.config.ConfigRenderOptions;
import io.dropwizard.Application;
import io.dropwizard.jackson.GuavaExtrasModule;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import org.apache.streams.config.StreamsConfiguration;
import org.apache.streams.config.StreamsConfigurator;
import org.apache.streams.core.StreamBuilder;
import org.apache.streams.core.StreamsDatum;
import org.apache.streams.core.StreamsPersistWriter;
import org.apache.streams.core.StreamsProcessor;
import org.apache.streams.core.StreamsProvider;
import org.apache.streams.jackson.StreamsJacksonMapper;
import org.apache.streams.local.builders.LocalStreamBuilder;
import org.apache.streams.pojo.json.Activity;
import org.joda.time.DateTime;
import org.reflections.Reflections;
import org.reflections.util.ConfigurationBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

import com.google.inject.Inject;

import javax.annotation.Resource;
import javax.ws.rs.Path;

public class StreamsApplication extends Application<StreamsDropwizardConfiguration> {

    private static final Logger LOGGER = LoggerFactory
			.getLogger(StreamsApplication.class);

    private static ObjectMapper mapper = StreamsJacksonMapper.getInstance();

    protected StreamBuilder builder;

    private static StreamsConfiguration streamsConfiguration;

    private Set<StreamsProvider> resourceProviders = Sets.newConcurrentHashSet();

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

        for( Class<?> resourceProviderClass : environment.jersey().getResourceConfig().getRootResourceClasses() ) {
            StreamsProvider provider = (StreamsProvider)resourceProviderClass.newInstance();
            if( StreamsProvider.class.isInstance(provider))
                resourceProviders.add(provider);
        }

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

    public StreamBuilder setup(StreamsConfiguration streamsConfiguration, Set<StreamsProvider> resourceProviders) {

        Map<String, Object> streamConfig = Maps.newHashMap();
        streamConfig.put(LocalStreamBuilder.TIMEOUT_KEY, 20 * 60 * 1000 * 1000);
        if(! Strings.isNullOrEmpty(streamsConfiguration.getBroadcastURI()) ) streamConfig.put("broadcastURI", streamsConfiguration.getBroadcastURI());
        StreamBuilder builder = new StreamDropwizardBuilder(1000, streamConfig);

        List<String> providers = new ArrayList<>();
        for( StreamsProvider provider: resourceProviders) {
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


    public static void main(String[] args) throws Exception
    {

        new StreamsApplication().run(args);

    }

}
