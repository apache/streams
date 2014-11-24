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

package org.apache.streams.facebook.serializer;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.base.Preconditions;
import org.apache.commons.lang.StringUtils;

import java.io.IOException;

import org.apache.streams.data.ActivityConverter;
import org.apache.streams.data.DocumentClassifier;
import org.apache.streams.facebook.Page;
import org.apache.streams.facebook.Post;
import org.apache.streams.facebook.api.FacebookPageActivityConverter;
import org.apache.streams.facebook.api.FacebookPostActivityConverter;
import org.apache.streams.jackson.StreamsJacksonMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FacebookDocumentClassifier implements DocumentClassifier {

    public FacebookDocumentClassifier() {

    }

    private static FacebookDocumentClassifier instance = new FacebookDocumentClassifier();

    public static FacebookDocumentClassifier getInstance() {
        return instance;
    }

    private final static Logger LOGGER = LoggerFactory.getLogger(FacebookDocumentClassifier.class);

    public Class detectClass(Object document) {

        Preconditions.checkNotNull(document);
        Preconditions.checkArgument(document instanceof String);

        String json = (String)document;
        Preconditions.checkArgument(StringUtils.isNotEmpty(json));

        ObjectNode objectNode;
        try {
            objectNode = (ObjectNode) StreamsJacksonMapper.getInstance().readTree(json);
        } catch (IOException e) {
            LOGGER.error("Exception while trying to detect class: {}", e.getMessage());
            return null;
        }

        if (objectNode.findValue("about") != null)
            return Page.class;
        else if (objectNode.findValue("statusType") != null)
            return Post.class;
        else
            return Post.class;
    }

}