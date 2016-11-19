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

package org.apache.streams.facebook.test.data;

import org.apache.streams.facebook.Page;
import org.apache.streams.jackson.StreamsJacksonMapper;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Joiner;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.input.BoundedInputStream;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;

/**
 * Tests serialization of Facebook Page inputs
 */
public class FacebookPageSerDeIT {

  private static final Logger LOGGER = LoggerFactory.getLogger(FacebookPageSerDeIT.class);
  private ObjectMapper mapper = StreamsJacksonMapper.getInstance();

  @Test
  public void Tests()
  {
    mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, Boolean.TRUE);
    mapper.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, Boolean.TRUE);
    mapper.configure(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT, Boolean.TRUE);

    InputStream is = FacebookPageSerDeIT.class.getResourceAsStream("/testpage.json");
    Joiner joiner = Joiner.on(" ").skipNulls();
    is = new BoundedInputStream(is, 10000);
    String json;

    try {
      json = joiner.join(IOUtils.readLines(is));
      LOGGER.debug(json);

      Page ser = mapper.readValue(json, Page.class);

      String de = mapper.writeValueAsString(ser);

      LOGGER.debug(de);

      Page serde = mapper.readValue(de, Page.class);

      Assert.assertEquals(ser, serde);

      LOGGER.debug(mapper.writeValueAsString(serde));

    } catch( Exception e ) {
      LOGGER.error("Exception: ", e);
      Assert.fail();
    }
  }
}