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

package org.apache.streams.facebook.provider;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigException;
import org.apache.streams.facebook.FacebookConfiguration;
import org.apache.streams.facebook.FacebookOAuthConfiguration;
import org.apache.streams.facebook.FacebookUserInformationConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class FacebookStreamConfigurator {
    private final static Logger LOGGER = LoggerFactory.getLogger(FacebookStreamConfigurator.class);
    private final static ObjectMapper mapper = new ObjectMapper();


    public static FacebookUserInformationConfiguration detectFacebookConfiguration(Config config) {
        FacebookUserInformationConfiguration facebookUserInformationConfiguration = new FacebookUserInformationConfiguration();

        try {
            Config oauth = config.getConfig("oauth");
            FacebookOAuthConfiguration facebookOAuthConfiguration = new FacebookOAuthConfiguration();
            facebookOAuthConfiguration.setAppAccessToken(oauth.getString("appAccessToken"));
            facebookOAuthConfiguration.setAppSecret(oauth.getString("appSecret"));
            facebookOAuthConfiguration.setUserAccessToken(oauth.getString("userAccessToken"));
            facebookOAuthConfiguration.setAppId(oauth.getString("appId"));

            facebookUserInformationConfiguration.setOauth(facebookOAuthConfiguration);
        } catch( ConfigException ce ) {}

        return facebookUserInformationConfiguration;
    }

    public static FacebookConfiguration detectConfiguration(Config config) {

        FacebookConfiguration facebookConfiguration = mapper.convertValue(detectFacebookConfiguration(config), FacebookConfiguration.class);

        return facebookConfiguration;
    }

    public static FacebookUserInformationConfiguration detectFacebookUserInformationConfiguration(Config config) {
        return mapper.convertValue(detectFacebookConfiguration(config), FacebookUserInformationConfiguration.class);
    }

}
