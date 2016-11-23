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

package org.apache.streams.pig;

import org.apache.streams.core.StreamsDatum;
import org.apache.streams.core.StreamsProcessor;
import org.apache.streams.jackson.StreamsJacksonMapper;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Preconditions;
import datafu.pig.util.SimpleEvalFunc;
import org.apache.commons.lang.ArrayUtils;
import org.apache.pig.builtin.MonitoredUDF;
import org.slf4j.Logger;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * UDF Wrapper at the Document level
 *
 * Useful when id,source,timestamp are not changing or you want to use pig
 * to modify them (rather than doing so within the processor)
 */
@MonitoredUDF(timeUnit = TimeUnit.SECONDS, duration = 30, intDefault = 10)
public class StreamsProcessDocumentExec extends SimpleEvalFunc<String> {

  private static final Logger LOGGER = org.slf4j.LoggerFactory.getLogger(StreamsProcessDocumentExec.class);

  StreamsProcessor streamsProcessor;
  ObjectMapper mapper = StreamsJacksonMapper.getInstance();

  public StreamsProcessDocumentExec(String... execArgs) throws ClassNotFoundException{
    Preconditions.checkNotNull(execArgs);
    Preconditions.checkArgument(execArgs.length > 0);
    String classFullName = execArgs[0];
    Preconditions.checkNotNull(classFullName);
    String[] prepareArgs = (String[]) ArrayUtils.remove(execArgs, 0);
    streamsProcessor = StreamsComponentFactory.getProcessorInstance(Class.forName(classFullName));
    if( execArgs.length == 1 ) {
      LOGGER.debug("prepare (null)");
      streamsProcessor.prepare(null);
    } else if( execArgs.length > 1 ) {
      LOGGER.debug("prepare " + Arrays.toString(prepareArgs));
      streamsProcessor.prepare(prepareArgs);
    }
  }

  public String call(String document) throws IOException {

    Preconditions.checkNotNull(streamsProcessor);
    Preconditions.checkNotNull(document);

    LOGGER.debug(document);

    StreamsDatum entry = new StreamsDatum(document);

    Preconditions.checkNotNull(entry);

    LOGGER.debug(entry.toString());

    List<StreamsDatum> resultSet = streamsProcessor.process(entry);

    LOGGER.debug(resultSet.toString());

    Object resultDoc = null;
    for( StreamsDatum resultDatum : resultSet ) {
      resultDoc = resultDatum.getDocument();
    }

    Preconditions.checkNotNull(resultDoc);

    if( resultDoc instanceof String )
      return (String) resultDoc;
    else
      return mapper.writeValueAsString(resultDoc);

  }

  public void finish() {
    streamsProcessor.cleanUp();
  }
}
