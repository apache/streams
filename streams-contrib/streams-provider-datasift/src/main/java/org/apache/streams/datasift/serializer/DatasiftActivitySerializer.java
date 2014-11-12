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

package org.apache.streams.datasift.serializer;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.streams.data.ActivitySerializer;
import org.apache.streams.datasift.Datasift;
import org.apache.streams.datasift.util.StreamsDatasiftMapper;
import org.apache.streams.exceptions.ActivitySerializerException;
import org.apache.streams.pojo.json.Activity;

import java.util.List;

/**
 *
 */
public class DatasiftActivitySerializer implements ActivitySerializer<Datasift> {

    private static final ObjectMapper MAPPER = StreamsDatasiftMapper.getInstance();

    @Override
    public String serializationFormat() {
        return null;
    }

    @Override
    public Datasift serialize(Activity deserialized) throws ActivitySerializerException {
        return null;
    }

    @Override
    public Activity deserialize(Datasift serialized) throws ActivitySerializerException {
        ActivitySerializer serializer = DatasiftEventClassifier.bestSerializer(serialized);
        return serializer.deserialize(serialized);
    }

    public Activity deserialize(String json) throws ActivitySerializerException {
        try {
            return deserialize(MAPPER.readValue(json, Datasift.class));
        } catch (Exception e) {
            throw new ActivitySerializerException(e);
        }
    }

    @Override
    public List<Activity> deserializeAll(List<Datasift> serializedList) {
        return null;
    }
}
