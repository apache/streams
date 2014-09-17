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

package org.apache.streams.twitter.provider;

import org.apache.streams.core.StreamsDatum;
import org.apache.streams.util.ComponentUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import twitter4j.*;

import java.util.List;

/**
 *  Retrieve recent posts for a single user id.
 */
public class TwitterTimelineProviderTask implements Runnable {

    private final static Logger LOGGER = LoggerFactory.getLogger(TwitterTimelineProviderTask.class);

    protected TwitterTimelineProvider provider;
    protected Twitter client;
    protected Long id;

    public TwitterTimelineProviderTask(TwitterTimelineProvider provider, Twitter twitter, Long id) {
        this.provider = provider;
        this.client = twitter;
        this.id = id;
    }

    @Override
    public void run() {

        Paging paging = new Paging(1, 200);
        List<Status> statuses = null;

        do
        {
            int keepTrying = 0;

            // keep trying to load, give it 5 attempts.
            //This value was chosen because it seemed like a reasonable number of times
            //to retry capturing a timeline given the sorts of errors that could potentially
            //occur (network timeout/interruption, faulty client, etc.)
            while (keepTrying < 5)
            {

                try
                {
                    this.client = provider.getTwitterClient();

                    statuses = client.getUserTimeline(id, paging);

                    for (Status tStat : statuses)
                    {
                        String json = TwitterObjectFactory.getRawJSON(tStat);

                        provider.addDatum(new StreamsDatum(json));

                    }

                    paging.setPage(paging.getPage() + 1);

                    keepTrying = 10;
                }
                catch(TwitterException twitterException) {
                    keepTrying += TwitterErrorHandler.handleTwitterError(client, twitterException);
                }
                catch(Exception e) {
                    keepTrying += TwitterErrorHandler.handleTwitterError(client, e);
                }
            }
        }
        while (provider.shouldContinuePulling(statuses));

        LOGGER.info(id + " Thread Finished");

    }

}
