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

package org.apache.streams.pig.test;

import org.apache.streams.core.StreamsDatum;
import org.apache.streams.core.StreamsProcessor;

import org.slf4j.Logger;

import java.util.LinkedList;
import java.util.List;

/**
 * Used to Test Pig processor wrapper when multiple datums are returned.
 */
public class CopyThriceProcessor implements StreamsProcessor {

  public final static String STREAMS_ID = "CopyThriceProcessor";

  private static final Logger LOGGER = org.slf4j.LoggerFactory.getLogger(CopyThriceProcessor.class);

  List<StreamsDatum> result;

  public CopyThriceProcessor() {
  }

  @Override
  public String getId() {
    return STREAMS_ID;
  }

  @Override
  public List<StreamsDatum> process(StreamsDatum entry) {
    this.result = new LinkedList<StreamsDatum>();
    result.add(entry);
    result.add(entry);
    result.add(entry);
    return result;
  }

  @Override
  public void prepare(Object configurationObject) {

  }

  @Override
  public void cleanUp() {
    LOGGER.info("Processor clean up");
  }
}
