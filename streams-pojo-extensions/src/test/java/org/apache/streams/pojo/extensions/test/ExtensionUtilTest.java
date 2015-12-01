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

package org.apache.streams.pojo.extensions.test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import org.apache.streams.jackson.StreamsJacksonMapper;
import org.apache.streams.pojo.extensions.ExtensionUtil;
import org.apache.streams.pojo.json.Activity;
import org.apache.streams.pojo.json.ActivityObject;
import org.junit.Test;

import java.util.Map;

/**
 *  Test ExtensionUtil methods
 */
public class ExtensionUtilTest {

    @Test
    public void testActivitySetCustomExtension() throws Exception {
        ExtensionUtil customExtensionUtil = ExtensionUtil.getInstance("ext");
        Activity activity = new Activity();
        Map<String, Object> extensions = customExtensionUtil.ensureExtensions(activity);
        String value = "value";
        extensions.put("extension", value);
        customExtensionUtil.setExtensions(activity, extensions);
        assert(!Strings.isNullOrEmpty((String)customExtensionUtil.getExtension(activity, "extension")));
        extensions = customExtensionUtil.getExtensions(activity);
        assert(value.equals((String)extensions.get("extension")));
        assert(activity.getAdditionalProperties().get("ext") != null);
    }

}
