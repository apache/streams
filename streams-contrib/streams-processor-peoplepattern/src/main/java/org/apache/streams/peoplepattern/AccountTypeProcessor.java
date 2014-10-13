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

package org.apache.streams.peoplepattern;

import com.google.common.collect.Maps;
import org.apache.streams.components.http.HttpConfigurator;
import org.apache.streams.components.http.HttpProcessorConfiguration;
import org.apache.streams.components.http.processor.SimpleHTTPGetProcessor;
import org.apache.streams.config.StreamsConfigurator;
import org.apache.streams.core.StreamsDatum;
import org.apache.streams.data.util.ExtensionUtil;
import org.apache.streams.pojo.json.Activity;
import org.apache.streams.pojo.json.ActivityObject;
import org.apache.streams.pojo.json.Actor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * Enrich actor with account type
 */
public class AccountTypeProcessor extends SimpleHTTPGetProcessor {

    private final static String STREAMS_ID = "AccountTypeProcessor";

    private final static Logger LOGGER = LoggerFactory.getLogger(AccountTypeProcessor.class);

    public AccountTypeProcessor() {
        this(HttpConfigurator.detectProcessorConfiguration(StreamsConfigurator.config.getConfig("peoplepattern")));
    }

    public AccountTypeProcessor(HttpProcessorConfiguration peoplePatternConfiguration) {
        super(peoplePatternConfiguration);
        LOGGER.info("creating AccountTypeProcessor");
        configuration.setProtocol("https");
        configuration.setHostname("api.peoplepattern.com");
        configuration.setResourcePath("/v0.2/account_type/");
        configuration.setEntity(HttpProcessorConfiguration.Entity.ACTOR);
        configuration.setExtension("account_type");
    }

    /**
     Override this to add parameters to the request
     */
    @Override
    protected Map<String, String> prepareParams(StreamsDatum entry) {
        Activity activity = mapper.convertValue(entry.getDocument(), Activity.class);
        Actor actor = activity.getActor();
        ActivityObject actorObject = mapper.convertValue(actor, ActivityObject.class);
        String username = (String) ExtensionUtil.getExtension(actorObject, "screenName");
        Map<String, String> params = Maps.newHashMap();
        params.put("id", actor.getId());
        params.put("name", actor.getDisplayName());
        params.put("username", username);
        params.put("description", actor.getSummary());
        return params;
    }
};
