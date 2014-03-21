package org.apache.streams.data.moreover;

import com.google.common.collect.Lists;
import com.typesafe.config.Config;
import org.apache.streams.moreover.MoreoverConfiguration;
import org.apache.streams.moreover.MoreoverKeyData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Created by sblackmon on 12/10/13.
 */
public class MoreoverConfigurator {

    private final static Logger LOGGER = LoggerFactory.getLogger(MoreoverConfigurator.class);

    public static MoreoverConfiguration detectConfiguration(Config moreover) {

        MoreoverConfiguration moreoverConfiguration = new MoreoverConfiguration();

        List<MoreoverKeyData> apiKeys = Lists.newArrayList();

        Config apiKeysConfig = moreover.getConfig("apiKeys");

        if( !apiKeysConfig.isEmpty())
            for( String apiKeyId : apiKeysConfig.root().keySet() ) {
                Config apiKeyConfig = apiKeysConfig.getConfig(apiKeyId);
                apiKeys.add(new MoreoverKeyData()
                        .withId(apiKeyConfig.getString("key"))
                        .withKey(apiKeyConfig.getString("key"))
                        .withStartingSequence(apiKeyConfig.getString("startingSequence")));
            }
        moreoverConfiguration.setApiKeys(apiKeys);

        return moreoverConfiguration;
    }

}
