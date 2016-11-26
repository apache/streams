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

import org.apache.streams.data.ActivityConverter;
import org.apache.streams.exceptions.ActivityConversionException;
import org.apache.streams.pojo.json.Activity;
import org.apache.streams.pojo.json.Provider;

import java.util.ArrayList;
import java.util.List;

/**
 * Support class for
 * {@link org.apache.streams.converter.test.CustomActivityConverterProcessorTest}
 */
public class CustomActivityConverter implements ActivityConverter<CustomType> {


    @Override
    public Class requiredClass() {
        return CustomType.class;
    }

    @Override
    public String serializationFormat() {
        return null;
    }

    @Override
    public CustomType fromActivity(Activity deserialized) throws ActivityConversionException {
        return null;
    }

    @Override
    public List<Activity> toActivityList(CustomType document) throws ActivityConversionException {
        Activity customActivity = new Activity();
        customActivity.setId(document.getTest());
        customActivity.setVerb(document.getTest());
        customActivity.setProvider((Provider)new Provider().withId(document.getTest()));
        List<Activity> activityList = new ArrayList<>();
        activityList.add(customActivity);
        return activityList;
    }

    @Override
    public List<Activity> toActivityList(List<CustomType> list) throws ActivityConversionException {
        return null;
    }

    @Override
    public List<CustomType> fromActivityList(List<Activity> list) throws ActivityConversionException {
        return null;
    }
}
