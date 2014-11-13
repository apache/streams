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

package org.apache.streams.twitter.serializer;

import org.apache.commons.lang.NotImplementedException;
import org.apache.streams.converter.TypeConverterUtil;
import org.apache.streams.data.ActivityConverter;
import org.apache.streams.data.ActivityConverterFactory;
import org.apache.streams.exceptions.ActivitySerializerException;
import org.apache.streams.pojo.json.Activity;

import java.util.List;
import java.io.Serializable;

/*
 * Now that we have ActivityConverterProcessor, this shouldn't be neededß
 */
@Deprecated
public class TwitterJsonActivityConverter implements ActivityConverter<String>, Serializable
{

    public TwitterJsonActivityConverter() {

    }

    private static TwitterJsonActivityConverter instance = new TwitterJsonActivityConverter();

    public static TwitterJsonActivityConverter getInstance() {
        return instance;
    }

    @Override
    public String serializationFormat() {
        return null;
    }

    @Override
    public String serialize(Activity deserialized) throws ActivitySerializerException {
        throw new NotImplementedException();
    }

    @Override
    public Activity deserialize(String serialized) throws ActivitySerializerException {

        Class converterClass = TwitterDocumentClassifier.getInstance().detectClass(serialized);

        ActivityConverter converter = ActivityConverterFactory.getInstance(converterClass);

        Object typedObject = TypeConverterUtil.convert(serialized, converterClass);

        Activity activity = converter.deserialize(typedObject);

        if( activity == null )
            throw new ActivitySerializerException("unrecognized type");

        return activity;
    }

    @Override
    public List<Activity> deserializeAll(List<String> serializedList) {
        throw new NotImplementedException();
    }
}
