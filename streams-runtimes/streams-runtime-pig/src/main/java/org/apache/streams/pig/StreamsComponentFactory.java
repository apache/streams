package org.apache.streams.pig;

/*
 * #%L
 * streams-runtime-pig
 * %%
 * Copyright (C) 2013 - 2014 Apache Streams Project
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import org.apache.commons.lang.ArrayUtils;
import org.apache.streams.core.StreamsProcessor;
import org.apache.streams.data.ActivitySerializer;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by sblackmon on 3/25/14.
 */
public class StreamsComponentFactory {

    public static ActivitySerializer getSerializerInstance(Class<?> serializerClazz) {

        Object object = null;
        try {
            object = serializerClazz.getConstructor().newInstance();
        } catch (Exception e) {
            e.printStackTrace();
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
            e.printStackTrace();
        }
        StreamsProcessor processor = (StreamsProcessor) object;
        return processor;

    }
}
