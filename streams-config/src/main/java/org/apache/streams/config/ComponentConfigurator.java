package org.apache.streams.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.reflect.TypeToken;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigRenderOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;

/**
 * Created by sblackmon on 11/11/14.
 */
public class ComponentConfigurator<T extends Serializable> {

    private Class<T> configClass;
    public ComponentConfigurator(Class<T> configClass) {
        this.configClass = configClass;
    }

    private final static Logger LOGGER = LoggerFactory.getLogger(ComponentConfigurator.class);

    private final static ObjectMapper mapper = new ObjectMapper();

    public T detectConfiguration(Config typesafeConfig) {

        T pojoConfig = null;

        try {
            pojoConfig = mapper.readValue(typesafeConfig.root().render(ConfigRenderOptions.concise()), configClass);
        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.warn("Could not parse:", typesafeConfig);
        }

        return pojoConfig;
    }

    public T detectConfiguration(String subConfig) {
        return detectConfiguration( StreamsConfigurator.config.getString(subConfig));
    }

    public T detectConfiguration(Config typesafeConfig, String subConfig) {
        return detectConfiguration( typesafeConfig.getString(subConfig));
    }
}
