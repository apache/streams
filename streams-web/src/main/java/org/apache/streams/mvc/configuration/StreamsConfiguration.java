package org.apache.streams.mvc.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class StreamsConfiguration {

    @Value("${baseUrlPath}")
    private String baseUrlPath;

    public String getBaseUrlPath() {
        return baseUrlPath;
    }

    public void setBaseUrlPath(String baseUrlPath) {
        this.baseUrlPath = baseUrlPath;
    }
}
