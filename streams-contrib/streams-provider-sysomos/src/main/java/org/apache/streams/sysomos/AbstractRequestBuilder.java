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

package org.apache.streams.sysomos;

import com.google.common.base.Strings;
import com.sysomos.xml.BeatApi;
import com.sysomos.xml.ObjectFactory;
import org.apache.commons.io.IOUtils;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Defines a common pattern for requesting data from the Sysomos API.
 */
public abstract class AbstractRequestBuilder implements RequestBuilder {

    protected static final Pattern CODE_PATTERN = Pattern.compile("code: ([0-9]+)");
    protected static final DateTimeFormatter SYSOMOS_DATE_FORMATTER = DateTimeFormat.forPattern("yyyy-MM-dd'T'hh:mm:ssZ");
    private final static Logger LOGGER = LoggerFactory.getLogger(AbstractRequestBuilder.class);


    /**
     * Executes the request to the Sysomos Heartbeat API and returns a valid response
     */
    public BeatApi.BeatResponse execute() {
        URL url = this.getRequestUrl();
        try {
            String xmlResponse = querySysomos(url);
            JAXBContext context = JAXBContext.newInstance(ObjectFactory.class);
            Unmarshaller unmarshaller = context.createUnmarshaller();
            BeatApi beatApi = (BeatApi) unmarshaller.unmarshal(new StringReader(xmlResponse));
            return beatApi.getBeatResponse();
        } catch (IOException e) {
            LOGGER.error("Error executing request : {}", e, url.toString());
            String message = e.getMessage();
            Matcher match = CODE_PATTERN.matcher(message);
            if(match.find()) {
                int errorCode = Integer.parseInt(match.group(1));
                throw new SysomosException(message, e, errorCode);
            }
            else {
                throw new SysomosException(e.getMessage(), e);
            }
        } catch (JAXBException e) {
            LOGGER.error("Unable to unmarshal XML content");
            throw new SysomosException("Unable to unmarshal XML content", e);
        }
    }

    protected String querySysomos(URL url) throws IOException {
        HttpURLConnection cn = (HttpURLConnection) url.openConnection();
        cn.setRequestMethod("GET");
        cn.addRequestProperty("Content-Type", "text/xml;charset=UTF-8");
        cn.setDoInput(true);
        cn.setDoOutput(false);
        StringWriter writer = new StringWriter();
        IOUtils.copy(new InputStreamReader(cn.getInputStream()), writer);
        writer.flush();

        String xmlResponse = writer.toString();
        if(Strings.isNullOrEmpty(xmlResponse)) {
            throw new SysomosException("XML Response from Sysomos was empty : "+xmlResponse+"\n"+cn.getResponseMessage(), cn.getResponseCode());
        }
        return xmlResponse;
    }

}
