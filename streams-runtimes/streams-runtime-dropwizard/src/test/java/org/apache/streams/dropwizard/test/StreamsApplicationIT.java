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

package org.apache.streams.dropwizard.test;

import com.google.common.collect.Lists;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;

/**
 * Tests {@link: org.apache.streams.dropwizard.StreamsApplication}
 */
public class StreamsApplicationIT {

  @BeforeClass
  public void setupTest() throws Exception {
    String[] testArgs = Lists.newArrayList("server", "src/test/resources/configuration.yml").toArray(new String[2]);
    TestStreamsApplication.main(testArgs);
  }

  @Test
  public void testApplicationStarted() throws Exception {

    final URL url = new URL("http://localhost:8003/admin/ping");
    final String response = new BufferedReader(new InputStreamReader(url.openStream())).readLine();
    Assert.assertEquals("pong", response);
  }
}
