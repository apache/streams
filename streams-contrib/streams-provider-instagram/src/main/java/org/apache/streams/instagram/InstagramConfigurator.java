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

package org.apache.streams.instagram;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Preconditions;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigRenderOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.io.IOException;

/**
 * Created by sblackmon on 12/10/13.
 */
public class InstagramConfigurator {

    private final static Logger LOGGER = LoggerFactory.getLogger(InstagramConfigurator.class);
    private final static ObjectMapper mapper = new ObjectMapper();


    public static InstagramConfiguration detectInstagramConfiguration(Config config) {

        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        Validator validator = factory.getValidator();

        InstagramConfiguration instagramConfiguration = null;
        try {
            instagramConfiguration = mapper.readValue(config.root().render(ConfigRenderOptions.concise()), InstagramConfiguration.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
        Preconditions.checkNotNull(instagramConfiguration);

        Preconditions.checkState(validator.validate(instagramConfiguration).size() == 0);

        return instagramConfiguration;
    }

    public static InstagramUserInformationConfiguration detectInstagramUserInformationConfiguration(Config config) {

        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        Validator validator = factory.getValidator();

        InstagramUserInformationConfiguration instagramConfiguration = null;
        try {
            instagramConfiguration = mapper.readValue(config.root().render(ConfigRenderOptions.concise()), InstagramUserInformationConfiguration.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
        Preconditions.checkNotNull(instagramConfiguration);

        Preconditions.checkState(validator.validate(instagramConfiguration).size() == 0);

        return instagramConfiguration;
    }

}
