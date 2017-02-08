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

package org.apache.streams.gplus.processor;

import org.apache.streams.core.StreamsDatum;
import org.apache.streams.core.StreamsProcessor;
import org.apache.streams.gplus.serializer.util.GooglePlusActivityUtil;
import org.apache.streams.pojo.json.Activity;

import com.google.api.services.plus.model.Comment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * GooglePlusCommentProcessor collects comments about a google plus activity.
 */
public class GooglePlusCommentProcessor implements StreamsProcessor {

  private static final String STREAMS_ID = "GooglePlusCommentProcessor";
  private static final Logger LOGGER = LoggerFactory.getLogger(GooglePlusCommentProcessor.class);
  private int count;

  @Override
  public String getId() {
    return STREAMS_ID;
  }

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
        /* TODO: FILL ME OUT WITH THE API CALL **/
        List<Comment> comments = new ArrayList<>();

        GooglePlusActivityUtil.updateActivity(comments, activity);
        result = new StreamsDatum(activity);
      }
    } catch (Exception ex) {
      ex.printStackTrace();
      LOGGER.error("Exception while converting Comment to Activity: {}", ex.getMessage());
    }

    if ( result != null ) {
      return Stream.of(result).collect(Collectors.toList());
    } else {
      return new ArrayList<>();
    }
  }

  @Override
  public void prepare(Object configurationObject) {
    count = 0;
  }

  @Override
  public void cleanUp() {

  }

  private String getGPlusID(String activityId) {
    String[] activityParts = activityId.split(":");
    return (activityParts.length > 0) ? activityParts[activityParts.length - 1] : "";
  }
}
