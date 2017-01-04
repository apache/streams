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

package org.apache.streams.sysomos.util;

import org.apache.streams.sysomos.SysomosException;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Provides utilities for working with Sysomos.
 */
public class SysomosUtils {

  private static final Logger LOGGER = LoggerFactory.getLogger(SysomosUtils.class);

  public static final Pattern CODE_PATTERN = Pattern.compile("code: ([0-9]+)");
  public static final DateTimeFormatter SYSOMOS_DATE_FORMATTER = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss'Z'").withZoneUTC();

  private SysomosUtils() {}

  /**
   * Queries the sysomos URL and provides the response as a String.
   *
   * @param url the Sysomos URL to query
   * @return valid XML String
   */
  public static String queryUrl(URL url) {
    try {
      HttpURLConnection cn = (HttpURLConnection) url.openConnection();
      cn.setRequestMethod("GET");
      cn.addRequestProperty("Content-Type", "text/xml;charset=UTF-8");
      cn.setDoInput(true);
      cn.setDoOutput(false);
      StringWriter writer = new StringWriter();
      IOUtils.copy(new InputStreamReader(cn.getInputStream()), writer);
      writer.flush();

      String xmlResponse = writer.toString();
      if (StringUtils.isEmpty(xmlResponse)) {
        throw new SysomosException("XML Response from Sysomos was empty : "
            + xmlResponse
            + "\n"
            + cn.getResponseMessage(),
            cn.getResponseCode());
      }
      return xmlResponse;
    } catch (IOException ex) {
      LOGGER.error("Error executing request : {}", ex, url.toString());
      String message = ex.getMessage();
      Matcher match = CODE_PATTERN.matcher(message);
      if (match.find()) {
        int errorCode = Integer.parseInt(match.group(1));
        throw new SysomosException(message, ex, errorCode);
      } else {
        throw new SysomosException(ex.getMessage(), ex);
      }
    }
  }
}
