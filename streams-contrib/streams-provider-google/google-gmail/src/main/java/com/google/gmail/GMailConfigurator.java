package com.google.gmail;

/*
 * #%L
 * google-gmail
 * %%
 * Copyright (C) 2013 - 2014 Apache Streams Project
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import com.google.gmail.GMailConfiguration;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigException;
import org.apache.streams.config.StreamsConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by sblackmon on 12/10/13.
 */
public class GMailConfigurator {

    private final static Logger LOGGER = LoggerFactory.getLogger(GMailConfigurator.class);

    public static GMailConfiguration detectConfiguration(Config gmail) {

        GMailConfiguration gmailConfiguration = new GMailConfiguration();

        gmailConfiguration.setUserName(gmail.getString("username"));
        gmailConfiguration.setPassword(gmail.getString("password"));

        return gmailConfiguration;
    }

}
