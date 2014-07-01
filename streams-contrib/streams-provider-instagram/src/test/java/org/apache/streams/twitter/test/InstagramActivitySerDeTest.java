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

package org.apache.streams.twitter.test;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang.StringUtils;
import org.apache.streams.instagram.serializer.util.InstagramDeserializer;
import org.apache.streams.instagram.serializer.InstagramJsonActivitySerializer;
import org.apache.streams.jackson.StreamsJacksonMapper;
import org.apache.streams.pojo.json.Activity;
import org.jinstagram.entity.users.feed.MediaFeedData;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

import static org.apache.streams.instagram.serializer.util.InstagramActivityUtil.updateActivity;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;

/**
* Created with IntelliJ IDEA.
* User: sblackmon
* Date: 8/20/13
* Time: 5:57 PM
* To change this template use File | Settings | File Templates.
*/
public class InstagramActivitySerDeTest {

    private final static Logger LOGGER = LoggerFactory.getLogger(InstagramActivitySerDeTest.class);

    @Test
    public void Tests() {
        InstagramDeserializer instagramDeserializer = new InstagramDeserializer("");
        InputStream is = InstagramActivitySerDeTest.class.getResourceAsStream("/testMediaFeedObjects.txt");
        InputStreamReader isr = new InputStreamReader(is);
        BufferedReader br = new BufferedReader(isr);

        try {
            while (br.ready()) {
                String line = br.readLine();
                if(!StringUtils.isEmpty(line))
                {
                    LOGGER.info("raw: {}", line);

                    MediaFeedData mediaFeedData = instagramDeserializer.createObjectFromResponse(MediaFeedData.class, line);

                    Activity activity = new Activity();

                    LOGGER.info("activity: {}", activity.toString());

                    updateActivity(mediaFeedData, activity);
                    assertThat(activity, is(not(nullValue())));

                    assertThat(activity.getId(), is(not(nullValue())));
                    assertThat(activity.getActor(), is(not(nullValue())));
                    assertThat(activity.getActor().getId(), is(not(nullValue())));
                    assertThat(activity.getVerb(), is(not(nullValue())));
                    assertThat(activity.getProvider(), is(not(nullValue())));
                }
            }
        } catch( Exception e ) {
            System.out.println(e);
            e.printStackTrace();
            Assert.fail();
        }
    }
}
