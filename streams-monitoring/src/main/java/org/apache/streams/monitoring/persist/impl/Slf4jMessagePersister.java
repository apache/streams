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

package org.apache.streams.monitoring.persist.impl;

import org.apache.streams.monitoring.persist.MessagePersister;

import org.slf4j.Logger;

import java.util.List;

/**
 * Persist montoring messages to SLF4J.
 */
public class Slf4jMessagePersister implements MessagePersister {

  private static final Logger LOGGER = org.slf4j.LoggerFactory.getLogger(Slf4jMessagePersister.class);
  private static final int SUCCESS_STATUS = 0;
  private static final int FAILURE_STATUS = -1;

  public Slf4jMessagePersister() {

  }

  @Override
  public int persistMessages(List<String> messages) {

    for (String message : messages) {
      LOGGER.info(message);
    }

    return SUCCESS_STATUS;
  }
}
