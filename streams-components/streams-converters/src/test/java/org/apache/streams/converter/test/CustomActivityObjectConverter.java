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

package org.apache.streams.converter.test;

import com.google.common.collect.Lists;
import org.apache.streams.data.ActivityConverter;
import org.apache.streams.data.ActivityObjectConverter;
import org.apache.streams.exceptions.ActivityConversionException;
import org.apache.streams.pojo.json.Activity;
import org.apache.streams.pojo.json.ActivityObject;
import org.apache.streams.pojo.json.Provider;

import java.util.List;

/**
 * Support class for
 * @see {@link CustomActivityObjectConverterProcessorTest}
 */
public class CustomActivityObjectConverter implements ActivityObjectConverter<CustomType> {

    @Override
    public Class requiredClass() {
        return CustomType.class;
    }

    @Override
    public String serializationFormat() {
        return null;
    }

    @Override
    public CustomType fromActivityObject(ActivityObject deserialized) throws ActivityConversionException {
        CustomType customType = new CustomType();
        customType.setTest(deserialized.getId());
        return customType;
    }

    @Override
    public ActivityObject toActivityObject(CustomType document) throws ActivityConversionException {
        ActivityObject customActivityObject = new ActivityObject();
        customActivityObject.setId(document.getTest());
        customActivityObject.setObjectType(document.getTest());
        return customActivityObject;
    }

}
