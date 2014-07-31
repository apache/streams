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

package org.apache.streams.instagram.processor;

import com.google.common.collect.Lists;
import org.apache.streams.core.StreamsDatum;
import org.apache.streams.core.StreamsProcessor;
import org.apache.streams.instagram.serializer.util.InstagramActivityUtil;
import org.apache.streams.pojo.json.Activity;
import org.jinstagram.entity.users.feed.MediaFeedData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Queue;

public class InstagramTypeConverter implements StreamsProcessor {

    public final static String STREAMS_ID = "InstagramTypeConverter";

    private final static Logger LOGGER = LoggerFactory.getLogger(InstagramTypeConverter.class);

    private Queue<MediaFeedData> inQueue;
    private Queue<StreamsDatum> outQueue;

    private InstagramActivityUtil instagramActivityUtil;

    private int count = 0;

    public final static String TERMINATE = new String("TERMINATE");

    public InstagramTypeConverter() {
    }

    public Queue<StreamsDatum> getProcessorOutputQueue() {
        return outQueue;
    }

    public void setProcessorInputQueue(Queue<MediaFeedData> inputQueue) {
        inQueue = inputQueue;
    }

    @Override
    public List<StreamsDatum> process(StreamsDatum entry) {

        StreamsDatum result = null;

        try {
            Object item = entry.getDocument();

            LOGGER.debug("{} processing {}", STREAMS_ID, item.getClass());

            if(item instanceof MediaFeedData) {
                //We don't need to use the mapper, since we have a process to convert between
                //MediaFeedData objects and Activity objects already
                Activity activity = new Activity();

                instagramActivityUtil.updateActivity((MediaFeedData)item, activity);

                if(activity.getId() != null) {
                    result = new StreamsDatum(activity);
                    count++;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.error("Exception while converting MediaFeedData to Activity: {}", e.getMessage());
        }

        if( result != null )
            return Lists.newArrayList(result);
        else
            return Lists.newArrayList();
    }

    @Override
    public void prepare(Object o) {
        instagramActivityUtil = new InstagramActivityUtil();
    }

    @Override
    public void cleanUp() {
        //noop
    }

}
