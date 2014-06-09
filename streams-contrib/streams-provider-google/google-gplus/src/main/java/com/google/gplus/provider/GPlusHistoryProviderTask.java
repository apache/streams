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

package com.google.gplus.provider;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.services.plus.Plus;
import com.google.api.services.plus.model.ActivityFeed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;

/**
 * Created by sblackmon on 12/10/13.
 */
public class GPlusHistoryProviderTask implements Runnable {

    private final static Logger LOGGER = LoggerFactory.getLogger(GPlusHistoryProviderTask.class);

    private ObjectMapper mapper;

    private GPlusProvider provider;
    private String userid;
    private String circle;

    public GPlusHistoryProviderTask(GPlusProvider provider, String userid, String circle) {
        this.provider = provider;
        this.userid = userid;
        this.circle = circle;
    }

    @Override
    public void run() {

        Plus.Activities.List listActivities = null;
        try {
            listActivities = provider.plus.activities().list(userid, circle);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        listActivities.setMaxResults(100L);

// Execute the request for the first page
        ActivityFeed activityFeed = null;
        try {
            activityFeed = listActivities.execute();
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

// Unwrap the request and extract the pieces we want
        List<com.google.api.services.plus.model.Activity> activities = activityFeed.getItems();

// Loop through until we arrive at an empty page
        while (activities != null) {
            for (com.google.api.services.plus.model.Activity gplusActivity : activities) {
                String json = null;
                try {
                    json = mapper.writeValueAsString(gplusActivity);
                } catch (JsonProcessingException e) {
                    e.printStackTrace();
                }
                provider.inQueue.offer(json);
            }

            // We will know we are on the last page when the next page token is null.
            // If this is the case, break.
            if (activityFeed.getNextPageToken() == null) {
                break;
            }

            // Prepare to request the next page of activities
            listActivities.setPageToken(activityFeed.getNextPageToken());

            // Execute and process the next page request
            try {
                activityFeed = listActivities.execute();
            } catch (IOException e) {
                e.printStackTrace();
            }
            activities = activityFeed.getItems();
        }

    }

}
