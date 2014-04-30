/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
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

package org.apache.streams.sysomos.provider;

import com.sysomos.xml.BeatApi;
import com.sysomos.xml.ObjectFactory;
import org.apache.streams.sysomos.SysomosException;
import org.apache.streams.sysomos.util.SysomosUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.StringReader;
import java.net.URL;

/**
 * Defines a common pattern for requesting data from the Sysomos API.
 */
public abstract class AbstractRequestBuilder implements RequestBuilder {
    private final static Logger LOGGER = LoggerFactory.getLogger(AbstractRequestBuilder.class);

    /**
     * Executes the request to the Sysomos Heartbeat API and returns a valid response
     */
    public BeatApi.BeatResponse execute() {
        URL url = this.getRequestUrl();
        try {
            String xmlResponse = SysomosUtils.queryUrl(url);
            JAXBContext context = JAXBContext.newInstance(ObjectFactory.class);
            Unmarshaller unmarshaller = context.createUnmarshaller();
            BeatApi beatApi = (BeatApi) unmarshaller.unmarshal(new StringReader(xmlResponse));
            return beatApi.getBeatResponse();
        } catch (JAXBException e) {
            LOGGER.error("Unable to unmarshal XML content");
            throw new SysomosException("Unable to unmarshal XML content", e);
        }
    }

}
