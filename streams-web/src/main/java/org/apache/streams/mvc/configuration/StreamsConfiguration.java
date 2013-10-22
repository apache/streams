package org.apache.streams.mvc.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.stereotype.Component;

@Configuration
@PropertySource("classpath:streams.properties")
public class StreamsConfiguration {

    @Bean
    public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
        return new PropertySourcesPlaceholderConfigurer();
    }

    @Value("${baseUrlPath}")
    private String baseUrlPath;

    public String getBaseUrlPath() {
        return baseUrlPath;
    }

    public void setBaseUrlPath(String baseUrlPath) {
        this.baseUrlPath = baseUrlPath;
    }
}
