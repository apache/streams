/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
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

package com.google.gplus.processor;

import com.google.api.client.util.Lists;
import com.google.api.services.plus.model.Comment;
import com.google.api.services.plus.model.Person;
import com.google.gplus.serializer.util.GPlusActivityDeserializer;
import com.google.gplus.serializer.util.GooglePlusActivityUtil;
import org.apache.streams.core.StreamsDatum;
import org.apache.streams.core.StreamsProcessor;
import org.apache.streams.pojo.json.Activity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class GooglePlusCommentProcessor implements StreamsProcessor {
    private final static String STREAMS_ID = "GooglePlusCommentProcessor";
    private final static Logger LOGGER = LoggerFactory.getLogger(GooglePlusCommentProcessor.class);
    private GooglePlusActivityUtil googlePlusActivityUtil;
    private int count;

    @Override
    public List<StreamsDatum> process(StreamsDatum entry) {
        StreamsDatum result = null;

        try {
            Object item = entry.getDocument();
            LOGGER.debug("{} processing {}", STREAMS_ID, item.getClass());

            //Get G+ activity ID from our own activity ID
            if (item instanceof Activity) {
                Activity activity = (Activity) item;
                String activityId = getGPlusID(activity.getId());

                //Call Google Plus API to get list of comments for this activity ID
                /**TODO: FILL ME OUT WITH THE API CALL**/
                List<Comment> comments = Lists.newArrayList();

                googlePlusActivityUtil.updateActivity(comments, activity);
                result = new StreamsDatum(activity);
            }
        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.error("Exception while converting Comment to Activity: {}", e.getMessage());
        }

        if( result != null )
            return com.google.common.collect.Lists.newArrayList(result);
        else
            return com.google.common.collect.Lists.newArrayList();
    }

    @Override
    public void prepare(Object configurationObject) {
        googlePlusActivityUtil = new GooglePlusActivityUtil();
        count = 0;
    }

    @Override
    public void cleanUp() {

    }

    private String getGPlusID(String activityID) {
        String[] activityParts = activityID.split(":");
        return (activityParts.length > 0) ? activityParts[activityParts.length - 1] : "";
    }
}
