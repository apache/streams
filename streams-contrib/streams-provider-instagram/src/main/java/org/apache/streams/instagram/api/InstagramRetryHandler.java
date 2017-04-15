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

package org.apache.streams.instagram.api;

import org.apache.streams.util.api.requests.backoff.AbstractBackOffStrategy;

import org.apache.juneau.rest.client.RetryOn;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *  Handle expected and unexpected exceptions.
 */
public class InstagramRetryHandler implements RetryOn {

  private static final Logger LOGGER = LoggerFactory.getLogger(InstagramRetryHandler.class);

  private static AbstractBackOffStrategy backoff_strategy;

  @Override
  public boolean onCode(int httpResponseCode) {
    if ( httpResponseCode > 400 ) {
      return true;
    } else {
      return false;
    }
  }
}
