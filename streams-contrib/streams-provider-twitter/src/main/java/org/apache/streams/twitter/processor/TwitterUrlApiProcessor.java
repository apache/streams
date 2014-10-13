package org.apache.streams.twitter.processor;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.streams.components.http.HttpProcessorConfiguration;
import org.apache.streams.components.http.processor.SimpleHTTPGetProcessor;
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
        this.configuration.setResourcePath("/1/urls/count.json");
        this.configuration.setEntity(HttpProcessorConfiguration.Entity.ACTIVITY);
        this.configuration.setExtension("twitter_url_count");
    }

    public TwitterUrlApiProcessor(HttpProcessorConfiguration processorConfiguration) {
        super(processorConfiguration);
        this.configuration.setHostname("urls.api.twitter.com");
        this.configuration.setResourcePath("/1/urls/count.json");
        this.configuration.setEntity(HttpProcessorConfiguration.Entity.ACTIVITY);
        this.configuration.setExtension("twitter_url_count");
    }

    @Override
    public List<StreamsDatum> process(StreamsDatum entry) {
        Preconditions.checkArgument(entry.getDocument() instanceof Activity);
        Activity activity = mapper.convertValue(entry.getDocument(), Activity.class);
        if( activity.getLinks() != null && activity.getLinks().size() > 0)
            return super.process(entry);
        else
            return Lists.newArrayList(entry);
    }

    @Override
    protected Map<String, String> prepareParams(StreamsDatum entry) {

        Map<String, String> params = Maps.newHashMap();

        params.put("url", mapper.convertValue(entry.getDocument(), Activity.class).getLinks().get(0));

        return params;
    }
}
