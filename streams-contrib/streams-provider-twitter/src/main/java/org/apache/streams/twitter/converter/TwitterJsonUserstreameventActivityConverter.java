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

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import org.apache.commons.lang.NotImplementedException;
import org.apache.streams.data.ActivityConverter;
import org.apache.streams.exceptions.ActivityConversionException;
import org.apache.streams.pojo.json.Activity;
import org.apache.streams.pojo.json.ActivityObject;
import org.apache.streams.twitter.pojo.UserstreamEvent;

import java.util.List;

import static org.apache.streams.twitter.converter.util.TwitterActivityUtil.formatId;
import static org.apache.streams.twitter.converter.util.TwitterActivityUtil.getProvider;


/**
* Created with IntelliJ IDEA.
* User: mdelaet
* Date: 9/30/13
* Time: 9:24 AM
* To change this template use File | Settings | File Templates.
*/
public class TwitterJsonUserstreameventActivityConverter implements ActivityConverter<UserstreamEvent> {

    public static Class requiredClass = UserstreamEvent.class;

    @Override
    public Class requiredClass() {
        return requiredClass;
    }

    private static TwitterJsonUserstreameventActivityConverter instance = new TwitterJsonUserstreameventActivityConverter();

    public static TwitterJsonUserstreameventActivityConverter getInstance() {
        return instance;
    }

    @Override
    public String serializationFormat() {
        return null;
    }

    @Override
    public UserstreamEvent fromActivity(Activity deserialized) throws ActivityConversionException {
        throw new NotImplementedException();
    }

    @Override
    public List<Activity> toActivityList(UserstreamEvent userstreamEvent) throws ActivityConversionException {

        Activity activity = convert(userstreamEvent);
        return Lists.newArrayList(activity);

    }

    @Override
    public List<UserstreamEvent> fromActivityList(List<Activity> list) {
        throw new NotImplementedException();
    }

    @Override
    public List<Activity> toActivityList(List<UserstreamEvent> serializedList) {
        return null;
    }

    public Activity convert(UserstreamEvent event) throws ActivityConversionException {

        Activity activity = new Activity();
        activity.setActor(buildActor(event));
        activity.setVerb(detectVerb(event));
        activity.setObject(buildActivityObject(event));
        activity.setId(formatId(activity.getVerb()));
        if(Strings.isNullOrEmpty(activity.getId()))
            throw new ActivityConversionException("Unable to determine activity id");
        activity.setProvider(getProvider());
        return activity;
    }

    public ActivityObject buildActor(UserstreamEvent event) {
        ActivityObject actor = new ActivityObject();
        //actor.setId(formatId(delete.getDelete().getStatus().getUserIdStr()));
        return actor;
    }

    public ActivityObject buildActivityObject(UserstreamEvent event) {
        ActivityObject actObj = new ActivityObject();
        //actObj.setId(formatId(delete.getDelete().getStatus().getIdStr()));
        //actObj.setObjectType("tweet");
        return actObj;
    }

    public String detectVerb(UserstreamEvent event) {
        return null;
    }

    public ActivityObject buildTarget(UserstreamEvent event) {
        return null;
    }

}
