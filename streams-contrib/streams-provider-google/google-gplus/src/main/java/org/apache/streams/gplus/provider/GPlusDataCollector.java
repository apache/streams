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

package org.apache.streams.gplus.provider;

import org.apache.streams.util.api.requests.backoff.BackOffException;
import org.apache.streams.util.api.requests.backoff.BackOffStrategy;

import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * GPlusDataCollector collects GPlus Data on behalf of providers.
 */
public abstract class GPlusDataCollector implements Runnable {

  private static final Logger LOGGER = LoggerFactory.getLogger(GPlusDataCollector.class);

  /**
   * Looks at the status code of the exception.  If the code indicates that the request should be retried,
   * it executes the back off strategy and returns true.
   * @param gjre GoogleJsonResponseException
   * @param backOff BackOffStrategy
   * @return returns true if the error code of the exception indicates the request should be retried.
   */
  public boolean backoffAndIdentifyIfRetry(GoogleJsonResponseException gjre, BackOffStrategy backOff) throws BackOffException {
    boolean tryAgain = false;
    switch (gjre.getStatusCode()) {
      case 400 :
        LOGGER.warn("Bad Request  : {}",  gjre);
        break;
      case 401 :
        LOGGER.warn("Invalid Credentials : {}", gjre);
        break;
      case 403 :
        LOGGER.warn("Possible rate limit exception. Retrying. : {}", gjre.getMessage());
        backOff.backOff();
        tryAgain = true;
        break;
      case 503 :
        LOGGER.warn("Google Backend Service Error : {}", gjre);
        break;
      default:
        LOGGER.warn("Google Service returned error : {}", gjre);
        tryAgain = true;
        backOff.backOff();
        break;
    }
    return tryAgain;
  }
  
}
