package org.apache.streams.jackson;

/*
 * #%L
 * streams-pojo
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

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import org.apache.streams.data.util.RFC3339Utils;
import org.joda.time.DateTime;
import org.joda.time.Period;

import java.io.IOException;
import java.io.Serializable;

public class StreamsPeriodSerializer extends StdSerializer<Period> implements Serializable
{
    protected StreamsPeriodSerializer(Class<Period> dateTimeClass) {
        super(dateTimeClass);
    }

    @Override
    public void serialize(Period value, JsonGenerator jgen, SerializerProvider provider) throws IOException
    {
        jgen.writeString(Integer.toString(value.getMillis()));
    }
}