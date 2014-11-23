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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Preconditions;
import datafu.pig.util.SimpleEvalFunc;
import org.apache.pig.builtin.MonitoredUDF;
import org.apache.streams.data.ActivityConverter;
import org.apache.streams.jackson.StreamsJacksonMapper;
import org.apache.streams.pojo.json.Activity;
import org.slf4j.Logger;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * UDF Wrapper for Serializers
 *
 * Just specify which Serializer with DEFINE - see tests for example
 */
@MonitoredUDF(timeUnit = TimeUnit.SECONDS, duration = 10, intDefault = 10)
public class StreamsSerializerExec extends SimpleEvalFunc<String> {

    private static final Logger LOGGER = org.slf4j.LoggerFactory.getLogger(StreamsSerializerExec.class);

    ActivityConverter activitySerializer;
    ObjectMapper mapper = StreamsJacksonMapper.getInstance();

    public StreamsSerializerExec(String... execArgs) throws ClassNotFoundException{
        Preconditions.checkNotNull(execArgs);
        Preconditions.checkArgument(execArgs.length > 0);
        String classFullName = execArgs[0];
        Preconditions.checkNotNull(classFullName);
        activitySerializer = StreamsComponentFactory.getSerializerInstance(Class.forName(classFullName));
    }

    public String call(String document) throws IOException {

        Preconditions.checkNotNull(activitySerializer);
        Preconditions.checkNotNull(document);

        Activity activity = null;
        try {
            activity = activitySerializer.deserialize(document);
        } catch( Exception e ) {
            LOGGER.warn(e.getMessage());
        }
        Preconditions.checkNotNull(activity);

        return mapper.writeValueAsString(activity);

    }

}
