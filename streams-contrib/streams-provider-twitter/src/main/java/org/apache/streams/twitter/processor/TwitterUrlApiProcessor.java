/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

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
 * Class gets a global share count from Twitter API for links on Activity datums
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
