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
public abstract class RequestBuilder {

    private static final Pattern CODE_PATTERN = Pattern.compile("code: ([0-9]+)");
    private final static Logger LOGGER = LoggerFactory.getLogger(RequestBuilder.class);

    /**
     * Returns the full url need to execute a request.
     * http://api.sysomos.com/dev/v1/heartbeat/content?apiKey=YOUR
     * -APIKEY&hid=YOUR-HEARTBEAT-ID&offset=0&size=10&
     * addedAfter=2010-10-15T13:00:00Z&addedBefore=2010-10-18T13:00:00Z
     *
     * @return the URL to use when making requests of Sysomos Heartbeat
     * @throws SysomosException
     * @throws java.net.MalformedURLException
     */
    protected abstract URL getFullRequestUrl() throws SysomosException, MalformedURLException;


    public String getFullRequestUrlString() throws SysomosException, MalformedURLException {
        return getFullRequestUrl().toString();
    }



    /**
     * Executes the request to the Sysomos Heartbeat API and returns a valid response
     *
     * @throws org.apache.streams.sysomos.SysomosException
     */
    public BeatApi.BeatResponse execute() throws SysomosException {
        URL url = getUrl();
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

    protected URL getUrl() throws SysomosException {
        URL url;
        try {
            url = this.getFullRequestUrl();
        } catch (MalformedURLException e1) {
            throw new SysomosException("Failed to construct the URL", e1);
        }
        return url;
    }

    protected String querySysomos(URL url) throws IOException, SysomosException {
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
