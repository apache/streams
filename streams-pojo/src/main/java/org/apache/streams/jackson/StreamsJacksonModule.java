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

package org.apache.streams.jackson;

import com.fasterxml.jackson.databind.module.SimpleModule;
import org.joda.time.DateTime;
import org.joda.time.Period;

import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * StreamsJacksonModule is a supporting class for
 * @see {@link org.apache.streams.jackson.StreamsJacksonMapper}
 *
 * RFC3339 dates are supported by default.
 */
public class StreamsJacksonModule extends SimpleModule {

    private final static Logger LOGGER = LoggerFactory.getLogger(StreamsJacksonModule.class);

    public StreamsJacksonModule() {
        super();

        addSerializer(DateTime.class, new StreamsDateTimeSerializer(DateTime.class));
        addDeserializer(DateTime.class, new StreamsDateTimeDeserializer(DateTime.class));

        addSerializer(Period.class, new StreamsPeriodSerializer(Period.class));
        addDeserializer(Period.class, new StreamsPeriodDeserializer(Period.class));
    }

    public StreamsJacksonModule(boolean scanForDateTimeFormats) {
        super();

        if(scanForDateTimeFormats) {
            Reflections reflections = new Reflections(new ConfigurationBuilder()
                    .setUrls(ClasspathHelper.forPackage("org.apache.streams.jackson"))
                    .setScanners(new SubTypesScanner()));

            Set<Class<? extends StreamsDateTimeFormat>> dateTimeFormatClasses = reflections.getSubTypesOf(StreamsDateTimeFormat.class);

            List<String> dateTimeFormats = new ArrayList<>();
            for (Class dateTimeFormatClass : dateTimeFormatClasses) {
                try {
                    dateTimeFormats.add(((StreamsDateTimeFormat) (dateTimeFormatClass.newInstance())).getFormat());
                } catch (Exception e) {
                    LOGGER.warn("Exception getting format from " + dateTimeFormatClass);
                }
            }

            addDeserializer(DateTime.class, new StreamsDateTimeDeserializer(DateTime.class, dateTimeFormats));
        } else {
            addDeserializer(DateTime.class, new StreamsDateTimeDeserializer(DateTime.class));
        }

        addSerializer(DateTime.class, new StreamsDateTimeSerializer(DateTime.class));

        addSerializer(Period.class, new StreamsPeriodSerializer(Period.class));
        addDeserializer(Period.class, new StreamsPeriodDeserializer(Period.class));
    }

    public StreamsJacksonModule(List < String > formats) {
        super();

        addSerializer(DateTime.class, new StreamsDateTimeSerializer(DateTime.class));
        addDeserializer(DateTime.class, new StreamsDateTimeDeserializer(DateTime.class, formats));

        addSerializer(Period.class, new StreamsPeriodSerializer(Period.class));
        addDeserializer(Period.class, new StreamsPeriodDeserializer(Period.class));
    }
}
