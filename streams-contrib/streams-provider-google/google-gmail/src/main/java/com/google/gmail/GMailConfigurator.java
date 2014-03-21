package com.google.gmail;

import com.google.gmail.GMailConfiguration;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigException;
import org.apache.streams.config.StreamsConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by sblackmon on 12/10/13.
 */
public class GMailConfigurator {

    private final static Logger LOGGER = LoggerFactory.getLogger(GMailConfigurator.class);

    public static GMailConfiguration detectConfiguration(Config gmail) {

        GMailConfiguration gmailConfiguration = new GMailConfiguration();

        gmailConfiguration.setUserName(gmail.getString("username"));
        gmailConfiguration.setPassword(gmail.getString("password"));

        return gmailConfiguration;
    }

}
