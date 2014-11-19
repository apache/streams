package org.apache.streams.dropwizard;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.guava.GuavaModule;
import com.fasterxml.jackson.module.afterburner.AfterburnerModule;
import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import com.hubspot.dropwizard.guice.GuiceBundle;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import com.typesafe.config.ConfigRenderOptions;
import io.dropwizard.Application;
import io.dropwizard.jackson.GuavaExtrasModule;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import org.apache.streams.config.StreamsConfigurator;
import org.apache.streams.console.ConsolePersistWriter;
import org.apache.streams.core.StreamBuilder;
import org.apache.streams.core.StreamsDatum;
import org.apache.streams.jackson.StreamsJacksonMapper;
import org.apache.streams.local.builders.LocalStreamBuilder;
import org.apache.streams.pojo.json.Activity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

public class StreamsApplication extends Application<StreamsDropwizardConfiguration> {

    private static final Logger LOGGER = LoggerFactory
			.getLogger(StreamsApplication.class);

    private static ObjectMapper mapper = StreamsJacksonMapper.getInstance();

    private StreamBuilder builder;

    private WebhookResource webhook;

    private String broadcastURI;

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

        webhook = new WebhookResource();

        executor = Executors.newSingleThreadExecutor();

        executor.execute(new StreamsDropwizardRunner());

        // wait for streams to start up
        Thread.sleep(10000);

        //environment.jersey().register(webhook);

    }

    private class StreamsDropwizardRunner implements Runnable {

        @Override
        public void run() {

            Map<String, Object> streamConfig = Maps.newHashMap();
            streamConfig.put(LocalStreamBuilder.TIMEOUT_KEY, 20 * 60 * 1000 * 1000);
            if(! Strings.isNullOrEmpty(broadcastURI) ) streamConfig.put("broadcastURI", broadcastURI);
            builder = new LocalStreamBuilder(1000, streamConfig);

            // prepare stream components
            builder.newPerpetualStream("webhooks", webhook);

            builder.addStreamsPersistWriter("console", new ConsolePersistWriter(), 1, "webhooks");
        }
    }


    public static void main(String[] args) throws Exception
    {

        StreamsApplication application = new StreamsApplication();
        if( args.length == 1 ) application.broadcastURI = args[0];
        application.run(args);

    }
}
