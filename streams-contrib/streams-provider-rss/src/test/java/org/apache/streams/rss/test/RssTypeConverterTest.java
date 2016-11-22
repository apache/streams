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

package org.apache.streams.rss.test;

import org.apache.streams.rss.processor.RssTypeConverter;

import org.apache.commons.lang3.SerializationUtils;
import org.junit.Test;

/**
 * Tests Serializability of {@link org.apache.streams.rss.processor.RssTypeConverter}
 */
public class RssTypeConverterTest {
  @Test
  public void testSerializability() {
    RssTypeConverter converter = new RssTypeConverter();
    RssTypeConverter clone = SerializationUtils.clone(converter);
  }
}
