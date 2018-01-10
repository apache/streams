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

import org.apache.streams.data.ActivityObjectConverter;
import org.apache.streams.instagram.pojo.UserInfo;
import org.apache.streams.instagram.serializer.InstagramUserInfoDataConverter;
import org.apache.streams.jackson.StreamsJacksonMapper;
import org.apache.streams.pojo.json.ActivityObject;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import org.apache.commons.lang.StringUtils;

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
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

/**
 * Tests conversion of instagram inputs to Activity.
 */
public class InstagramUserInfoDataConverterIT {

  private static final Logger LOGGER = LoggerFactory.getLogger(InstagramUserInfoDataConverterIT.class);

  private ObjectMapper mapper = StreamsJacksonMapper.getInstance();

  @Test(dependsOnGroups = "InstagramUserInfoProviderIT")
  public void InstagramUserInfoDataConverterIT() throws Exception {
    InputStream is = InstagramUserInfoDataConverterIT.class.getResourceAsStream("/InstagramUserInfoProviderIT.stdout.txt");
    InputStreamReader isr = new InputStreamReader(is);
    BufferedReader br = new BufferedReader(isr);

    PrintStream outStream = new PrintStream(
        new BufferedOutputStream(
            new FileOutputStream("target/test-classes/InstagramUserInfoDataConverterIT.txt")));

    try {
      while (br.ready()) {
        String line = br.readLine();
        if (!StringUtils.isEmpty(line)) {

          LOGGER.info("raw: {}", line);

          UserInfo userInfoData = mapper.readValue(line, UserInfo.class);

          ActivityObjectConverter<UserInfo> converter = new InstagramUserInfoDataConverter();

          ActivityObject activityObject = converter.toActivityObject(userInfoData);

          LOGGER.info("activityObject: {}", activityObject.toString());

          assertThat(activityObject, is(not(nullValue())));

          assertThat(activityObject.getId(), is(not(nullValue())));
          assertThat(activityObject.getImage(), is(not(nullValue())));
          assertThat(activityObject.getDisplayName(), is(not(nullValue())));
          assertThat(activityObject.getSummary(), is(not(nullValue())));

          Map<String, Object> extensions = (Map<String, Object>)activityObject.getAdditionalProperties().get("extensions");
          assertThat(extensions, is(not(nullValue())));
          assertThat(extensions.get("following"), is(not(nullValue())));
          assertThat(extensions.get("followers"), is(not(nullValue())));
          assertThat(extensions.get("screenName"), is(not(nullValue())));
          assertThat(extensions.get("posts"), is(not(nullValue())));

          assertThat(activityObject.getAdditionalProperties().get("handle"), is(not(nullValue())));
          assertThat(activityObject.getId(), is(not(nullValue())));
          assertThat(activityObject.getUrl(), is(not(nullValue())));

          assertThat(activityObject.getAdditionalProperties().get("provider"), is(not(nullValue())));

          outStream.println(mapper.writeValueAsString(activityObject));

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
