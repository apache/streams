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

package org.apache.streams.sysomos.test;

import org.apache.streams.sysomos.Sysomos;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Tests ability to convert String json form to {@link org.apache.streams.sysomos.Sysomos} form
 */
public class SysomosJsonSerDeIT {

  private static final Logger LOGGER = LoggerFactory.getLogger(SysomosJsonSerDeIT.class);

  private ObjectMapper mapper = new ObjectMapper();

  @Test
  public void testSysomosJsonSerDe() {

    mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, Boolean.FALSE);
    mapper.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, Boolean.TRUE);
    mapper.configure(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT, Boolean.TRUE);

    InputStream is = SysomosJsonSerDeIT.class.getResourceAsStream("/sysomos_jsons.txt");
    InputStreamReader isr = new InputStreamReader(is);
    BufferedReader br = new BufferedReader(isr);

    try {
      while (br.ready()) {
        String line = br.readLine();
        LOGGER.debug(line);

        Sysomos ser = mapper.readValue(line, Sysomos.class);

        String des = mapper.writeValueAsString(ser);
        LOGGER.debug(des);
      }
    } catch (Exception ex) {
      ex.printStackTrace();
      Assert.fail();
    }
  }
}
