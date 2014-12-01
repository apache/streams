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

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import org.apache.commons.lang.ArrayUtils;
import org.apache.streams.core.StreamsProcessor;
import org.apache.streams.data.ActivitySerializer;
import org.slf4j.Logger;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Static reflection wrappers for instantiating StreamsComponents
 */
public class StreamsComponentFactory {

    private static final Logger LOGGER = org.slf4j.LoggerFactory.getLogger(StreamsComponentFactory.class);

    public static ActivitySerializer getSerializerInstance(Class<?> serializerClazz) {

        Object object = null;
        try {
            object = serializerClazz.getConstructor().newInstance();
        } catch (Exception e) {
            LOGGER.error(e.getMessage());
        }

        Preconditions.checkNotNull(object);

        ActivitySerializer serializer = (ActivitySerializer) object;

        return serializer;

    }

    public static StreamsProcessor getProcessorInstance(Class<?> processorClazz) {

        Object object = null;
        try {
            object = processorClazz.getConstructor().newInstance();
        } catch (Exception e) {
            LOGGER.error(e.getMessage());
        }
        StreamsProcessor processor = (StreamsProcessor) object;
        return processor;

    }

}
