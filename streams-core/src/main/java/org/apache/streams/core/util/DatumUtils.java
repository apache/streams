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

package org.apache.streams.core.util;

import org.apache.streams.core.StreamsDatum;
import org.apache.streams.core.StreamsOperation;

import org.joda.time.DateTime;

import java.util.HashMap;
import java.util.Map;

/**
 * Provides common utilities for managing and manipulating StreamsDatums.
 */
public class DatumUtils {

  /**
   * Adds an error occurred during a StreamsOperation step to the StreamsDatum's metadata.  By convention, errors are
   * placed in the metadata under the "errors" and are organized by class simple name where the failure occurred.
   *
   * @param datum the datum on which the operation step errored
   * @param throwable the throwable encountered
   * @param operationClass the class of the operation
   */
  @SuppressWarnings("all")
  public static void addErrorToMetadata(StreamsDatum datum, Throwable throwable, Class<? extends StreamsOperation> operationClass) {
    if (!datum.getMetadata().containsKey("errors")) {
      datum.getMetadata().put("errors", new HashMap<String, Throwable>());
    }
    Map<String, Throwable> errors = (Map)datum.getMetadata().get("errors");
    errors.put(operationClass.getCanonicalName(), throwable);
  }

  public static StreamsDatum cloneDatum(StreamsDatum datum) {
    StreamsDatum clone = new StreamsDatum(datum.getDocument());
    clone.setId(datum.getId() == null ? null : datum.getId());
    clone.setTimestamp(datum.getTimestamp() == null ? null : new DateTime(datum.getTimestamp()));
    clone.setSequenceid(datum.getSequenceid() == null ? null : datum.getSequenceid());
    clone.setMetadata(datum.getMetadata() == null ? null : new HashMap<>(datum.getMetadata()));
    return clone;
  }
}
