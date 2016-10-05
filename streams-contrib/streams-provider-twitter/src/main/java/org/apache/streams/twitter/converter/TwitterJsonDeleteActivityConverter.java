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

package org.apache.streams.twitter.converter;

import com.google.common.collect.Lists;
import org.apache.commons.lang.NotImplementedException;
import org.apache.streams.data.ActivityConverter;
import org.apache.streams.exceptions.ActivityConversionException;
import org.apache.streams.pojo.json.Activity;
import org.apache.streams.pojo.json.ActivityObject;
import org.apache.streams.twitter.pojo.Delete;
import org.apache.streams.twitter.pojo.Tweet;

import java.io.Serializable;
import java.util.List;

import static org.apache.streams.twitter.converter.util.TwitterActivityUtil.updateActivity;


/**
* Created with IntelliJ IDEA.
* User: mdelaet
* Date: 9/30/13
* Time: 9:24 AM
* To change this template use File | Settings | File Templates.
*/
public class TwitterJsonDeleteActivityConverter implements ActivityConverter<Delete>, Serializable {

    public static Class requiredClass = Delete.class;

    @Override
    public Class requiredClass() {
        return requiredClass;
    }

    private static TwitterJsonDeleteActivityConverter instance = new TwitterJsonDeleteActivityConverter();

    public static TwitterJsonDeleteActivityConverter getInstance() {
        return instance;
    }

    @Override
    public String serializationFormat() {
        return null;
    }

    @Override
    public Delete fromActivity(Activity deserialized) throws ActivityConversionException {
        throw new NotImplementedException();
    }

    @Override
    public List<Activity> toActivityList(List<Delete> serializedList) {
        throw new NotImplementedException();
    }

    public List<Activity> toActivityList(Delete delete) throws ActivityConversionException {

        Activity activity = new Activity();
        updateActivity(delete, activity);
        return Lists.newArrayList(activity);
    }

    @Override
    public List<Delete> fromActivityList(List<Activity> list) {
        throw new NotImplementedException();
    }

    public ActivityObject buildTarget(Tweet tweet) {
        return null;
    }

}
