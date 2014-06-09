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

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import twitter4j.Paging;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.json.DataObjectFactory;

import java.util.List;

/**
 * Created by sblackmon on 12/10/13.
 */
public class TwitterTimelineProviderTask implements Runnable {

    private final static Logger LOGGER = LoggerFactory.getLogger(TwitterTimelineProviderTask.class);

    private TwitterTimelineProvider provider;
    private Twitter twitter;
    private Long id;

    public TwitterTimelineProviderTask(TwitterTimelineProvider provider, Twitter twitter, Long id) {
        this.provider = provider;
        this.twitter = twitter;
        this.id = id;
    }

    @Override
    public void run() {

        Paging paging = new Paging(1, 200);
        List<Status> statuses = null;
        boolean KeepGoing = true;
        boolean hadFailure = false;

        do
        {
            int keepTrying = 0;

            // keep trying to load, give it 5 attempts.
            //while (keepTrying < 10)
            while (keepTrying < 1)
            {

                try
                {
                    statuses = twitter.getUserTimeline(id, paging);

                    for (Status tStat : statuses)
                    {
                        if( provider.start != null &&
                            provider.start.isAfter(new DateTime(tStat.getCreatedAt())))
                        {
                            // they hit the last date we wanted to collect
                            // we can now exit early
                            KeepGoing = false;
                        }
                        // emit the record
                        String json = DataObjectFactory.getRawJSON(tStat);

                        //provider.offer(json);

                    }


                    paging.setPage(paging.getPage() + 1);

                    keepTrying = 10;
                }
                catch(Exception e)
                {
                    hadFailure = true;
                    keepTrying += TwitterErrorHandler.handleTwitterError(twitter, e);
                }
            }
        }
        while ((statuses != null) && (statuses.size() > 0) && KeepGoing);

        LOGGER.info("Provider Finished.  Cleaning up...");

        LOGGER.info("Provider Exiting");

    }

}
