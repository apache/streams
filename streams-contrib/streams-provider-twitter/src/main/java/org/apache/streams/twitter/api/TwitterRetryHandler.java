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

package org.apache.streams.twitter.api;

import org.apache.streams.util.api.requests.backoff.AbstractBackOffStrategy;
import org.apache.streams.util.api.requests.backoff.BackOffException;
import org.apache.streams.util.api.requests.backoff.impl.LinearTimeBackOffStrategy;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.apache.juneau.json.JsonParser;
import org.apache.juneau.parser.ParseException;
import org.apache.juneau.rest.client.RetryOn;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.ws.rs.core.Response;

/**
 *  Handle expected and unexpected exceptions.
 */
public class TwitterRetryHandler extends RetryOn {

  private static final Logger LOGGER = LoggerFactory.getLogger(TwitterRetryHandler.class);

  protected boolean onResponse(HttpResponse response) {
    LOGGER.debug(response.toString());
    switch(response.getStatusLine().getStatusCode()) {
      case 200: // Response.Status.OK
      case 304: // Response.Status.NOT_MODIFIED
      case 400: // Response.Status.BAD_REQUEST
        return false;
      case 401: // Response.Status.UNAUTHORIZED
        return true;
      case 403: // Response.Status.FORBIDDEN
      case 404: // Response.Status.NOT_FOUND
      case 406: // Response.Status.NOT_ACCEPTABLE
      case 410: // Response.Status.GONE
        return false;
      case 420: // Enhance Your Calm
      case 429: // Too Many Requests
        return true;
      case 500: // Response.Status.INTERNAL_SERVER_ERROR
      case 502: // Bad Gateway
      case 503: // Response.Status.SERVICE_UNAVAILABLE
      case 504: // Gateway Timeout
        return true;
      default:
        return false;
    }
  }

  private ResponseErrors parseResponseErrors(HttpResponse response) {
    ResponseErrors responseErrors = null;
    if (response.getEntity() != null) {
      try {
        String responseEntity = EntityUtils.toString(response.getEntity());
        LOGGER.debug(responseEntity);
        responseErrors = JsonParser.DEFAULT.parse(responseEntity, ResponseErrors.class);
      } catch (IOException ex) {
        LOGGER.error("IOException", ex);
      } catch (ParseException ex) {
        LOGGER.error("ParseException", ex);
      }
    }
    return responseErrors;
  }
}
