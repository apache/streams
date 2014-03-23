package org.apache.streams.datasift.provider;

import com.typesafe.config.Config;
import org.apache.streams.datasift.DatasiftConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by sblackmon on 12/10/13.
 */
public class DatasiftStreamConfigurator {

    private final static Logger LOGGER = LoggerFactory.getLogger(DatasiftStreamConfigurator.class);

    public static DatasiftConfiguration detectConfiguration(Config datasift) {

        DatasiftConfiguration datasiftConfiguration = new DatasiftConfiguration();

        datasiftConfiguration.setApiKey(datasift.getString("apiKey"));
        datasiftConfiguration.setUserName(datasift.getString("userName"));
        datasiftConfiguration.setStreamHash(datasift.getStringList("hashes"));

        return datasiftConfiguration;
    }

}
