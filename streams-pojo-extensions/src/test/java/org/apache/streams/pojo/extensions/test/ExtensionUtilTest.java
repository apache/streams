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

package org.apache.streams.pojo.extensions;

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

    ObjectMapper mapper = StreamsJacksonMapper.getInstance();

    /**
     * Test promoteExtensions(Activity)
     */
    @Test
    public void testActivityPromoteExtensions() throws Exception {
        Activity activity = new Activity();
        Map<String, Object> extensions = ExtensionUtil.ensureExtensions(activity);
        extensions.put("extension", "value");
        ExtensionUtil.setExtensions(activity, extensions);
        assert(!Strings.isNullOrEmpty((String)ExtensionUtil.getExtension(activity, "extension")));
        ExtensionUtil.promoteExtensions(activity);
        extensions = ExtensionUtil.getExtensions(activity);
        assert(extensions.size() == 0);
        assert(activity.getAdditionalProperties().get("extension").equals("value"));
    }

    /**
     * Test promoteExtensions(ActivityObject)
     */
    @Test
    public void testActivityObjectPromoteExtensions() throws Exception {
        ActivityObject activityObject = new ActivityObject();
        Map<String, Object> extensions = ExtensionUtil.ensureExtensions(activityObject);
        extensions.put("extension", "value");
        ExtensionUtil.setExtensions(activityObject, extensions);
        assert(!Strings.isNullOrEmpty((String)ExtensionUtil.getExtension(activityObject, "extension")));
        ExtensionUtil.promoteExtensions(activityObject);
        extensions = ExtensionUtil.getExtensions(activityObject);
        assert(extensions.size() == 0);
        assert(activityObject.getAdditionalProperties().get("extension").equals("value"));
    }

}
