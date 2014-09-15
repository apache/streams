package org.apache.streams.twitter.processor;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import org.apache.streams.components.http.HttpProcessorConfiguration;
import org.apache.streams.components.http.SimpleHTTPGetProcessor;
import org.apache.streams.config.StreamsConfigurator;
import org.apache.streams.core.StreamsDatum;
import org.apache.streams.core.StreamsProcessor;
import org.apache.streams.pojo.json.Activity;

import java.util.List;
import java.util.Map;

/**
 * Created by sblackmon on 9/14/14.
 */
public class TwitterUrlApiProcessor extends SimpleHTTPGetProcessor implements StreamsProcessor {

    public TwitterUrlApiProcessor() {
        super();
        this.configuration.setHostname("urls.api.twitter.com");
        this.configuration.setResourceUri("/1/urls/count.json");
        this.configuration.setExtension("twitter_url_count");
    }

    public TwitterUrlApiProcessor(HttpProcessorConfiguration processorConfiguration) {
        super(processorConfiguration);
        this.configuration.setHostname("urls.api.twitter.com");
        this.configuration.setResourceUri("/1/urls/count.json");
        this.configuration.setExtension("twitter_url_count");
    }

    @Override
    public List<StreamsDatum> process(StreamsDatum entry) {
        Preconditions.checkArgument(entry.getDocument() instanceof Activity);
        Activity activity = mapper.convertValue(entry, Activity.class);
        Preconditions.checkArgument(!Strings.isNullOrEmpty(activity.getUrl()));
        return super.process(entry);
    }

    @Override
    protected Map<String, String> prepareParams(StreamsDatum entry) {

        Map<String, String> params = Maps.newHashMap();

        params.put("url", mapper.convertValue(entry, Activity.class).getUrl());

        return params;
    }
}
