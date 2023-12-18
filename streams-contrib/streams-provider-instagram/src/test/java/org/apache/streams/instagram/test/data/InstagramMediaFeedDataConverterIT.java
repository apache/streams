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

package org.apache.streams.instagram.test.data;

import org.apache.streams.data.ActivityConverter;
import org.apache.streams.instagram.pojo.Media;
import org.apache.streams.instagram.serializer.InstagramMediaFeedDataConverter;
import org.apache.streams.jackson.StreamsJacksonMapper;
import org.apache.streams.pojo.json.Activity;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

/**
 * Tests conversion of instagram inputs to Activity.
 */
public class InstagramMediaFeedDataConverterIT {

  private static final Logger LOGGER = LoggerFactory.getLogger(InstagramMediaFeedDataConverterIT.class);

  private ObjectMapper mapper = StreamsJacksonMapper.getInstance();

  @Test(dependsOnGroups = "InstagramRecentMediaProviderIT")
  public void InstagramMediaFeedDataConverterIT() throws Exception {
    InputStream is = InstagramMediaFeedDataConverterIT.class.getResourceAsStream("/InstagramRecentMediaProviderIT.stdout.txt");
    InputStreamReader isr = new InputStreamReader(is);
    BufferedReader br = new BufferedReader(isr);

    PrintStream outStream = new PrintStream(
        new BufferedOutputStream(
            new FileOutputStream("target/test-classes/InstagramMediaFeedDataConverterIT.txt")));

    try {
      while (br.ready()) {
        String line = br.readLine();
        if (!StringUtils.isEmpty(line)) {
          LOGGER.info("raw: {}", line);

          Media mediaFeedData = mapper.readValue(line, Media.class);

          ActivityConverter<Media> converter = new InstagramMediaFeedDataConverter();

          Activity activity = converter.toActivityList(mediaFeedData).get(0);

          LOGGER.info("activity: {}", activity.toString());

          assertThat(activity, is(not(nullValue())));

          assertThat(activity.getId(), is(not(nullValue())));
          assertThat(activity.getActor(), is(not(nullValue())));
          assertThat(activity.getActor().getId(), is(not(nullValue())));
          assertThat(activity.getVerb(), is(not(nullValue())));
          assertThat(activity.getProvider(), is(not(nullValue())));

          outStream.println(mapper.writeValueAsString(activity));

        }

      }
      outStream.flush();

    } catch ( Exception ex ) {
      LOGGER.error("Exception: ", ex);
      outStream.flush();
      Assert.fail();
    }
  }

}
