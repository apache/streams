/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
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

package org.apache.streams.twitter.serializer.util;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import org.apache.streams.pojo.json.Activity;
import org.apache.streams.pojo.json.Provider;

import java.util.Map;

/**
 * Provides utilities for working with Activity objects within the context of Twitter
 */
public class TwitterActivityUtil {


    public static Provider getProvider() {
        Provider provider = new Provider();
        provider.setId("id:providers:twitter");
        provider.setDisplayName("Twitter");
        return provider;
    }

    public static void addTwitterExtension(Activity activity, ObjectNode event) {
        Map<String, Object> extensions = org.apache.streams.data.util.ActivityUtil.ensureExtensions(activity);
        extensions.put("twitter", event);
    }

    public static String formatId(String... idparts) {
        return Joiner.on(":").join(Lists.asList("id:twitter", idparts));
    }
}
