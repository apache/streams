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

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import org.joda.time.Period;

import java.io.IOException;
import java.io.Serializable;

public class StreamsPeriodDeserializer extends StdDeserializer<Period> implements Serializable
{

    protected StreamsPeriodDeserializer(Class<Period> dateTimeClass) {
        super(dateTimeClass);
    }

    public Period deserialize(JsonParser jpar, DeserializationContext context) throws IOException {
        return Period.millis(jpar.getIntValue());
    }
}
