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

import java.util.List;

/**
 * Created by sblackmon on 3/27/14.
 */
public class StreamsJacksonModule extends SimpleModule {

    public StreamsJacksonModule() {
        super();
        addSerializer(DateTime.class, new StreamsDateTimeSerializer(DateTime.class));
        addDeserializer(DateTime.class, new StreamsDateTimeDeserializer(DateTime.class));

        addSerializer(Period.class, new StreamsPeriodSerializer(Period.class));
        addDeserializer(Period.class, new StreamsPeriodDeserializer(Period.class));
    }

    public StreamsJacksonModule(List<String> formats) {
        super();
        addSerializer(DateTime.class, new StreamsDateTimeSerializer(DateTime.class));
        addDeserializer(DateTime.class, new StreamsDateTimeDeserializer(DateTime.class, formats));

        addSerializer(Period.class, new StreamsPeriodSerializer(Period.class));
        addDeserializer(Period.class, new StreamsPeriodDeserializer(Period.class));
    }
}
