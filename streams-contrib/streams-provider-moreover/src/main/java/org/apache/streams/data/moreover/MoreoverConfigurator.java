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

        for( String key : moreover.getStringList("apiKeys")) {
            apiKeys.add(new MoreoverKeyData().withId(key).withKey(key));
            // TODO: implement starting sequence
        }
        moreoverConfiguration.setApiKeys(apiKeys);

        return moreoverConfiguration;
    }

}
