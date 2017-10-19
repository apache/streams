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

package org.apache.streams.moreover.test;

import org.apache.streams.data.ActivitySerializer;
import org.apache.streams.moreover.MoreoverTestUtil;
import org.apache.streams.moreover.MoreoverXmlActivitySerializer;
import org.apache.streams.pojo.json.Activity;

import org.apache.commons.io.IOUtils;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.List;

/**
 * Tests ability to serialize moreover xml Strings
 *
 * Disabled until credentials, provider and provider IT capable of generating fresh data are available.
 */
public class MoreoverXmlActivitySerializerIT {
  ActivitySerializer serializer;
  private String xml;

  @BeforeClass
  public void setup() throws IOException {
    serializer = new MoreoverXmlActivitySerializer();
    xml = loadXml();
  }

  @Test(enabled = false)
  public void loadData() throws Exception {
    List<Activity> activities = serializer.deserializeAll(Collections.singletonList(xml));
    for (Activity activity : activities) {
      MoreoverTestUtil.validate(activity);
    }
  }

  private String loadXml() throws IOException {
    StringWriter writer = new StringWriter();
    InputStream resourceAsStream = this.getClass().getResourceAsStream("/moreover.xml");
    IOUtils.copy(resourceAsStream, writer, Charset.forName("UTF-8"));
    return writer.toString();
  }

}
