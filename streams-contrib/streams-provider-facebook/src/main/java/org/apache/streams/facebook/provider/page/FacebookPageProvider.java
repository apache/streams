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
package org.apache.streams.facebook.provider.page;

import com.google.common.annotations.VisibleForTesting;
import org.apache.streams.core.StreamsDatum;
import org.apache.streams.facebook.FacebookConfiguration;
import org.apache.streams.facebook.provider.FacebookDataCollector;
import org.apache.streams.facebook.provider.FacebookProvider;

import java.util.concurrent.BlockingQueue;

/**
 * Streams Provider which aggregates Page Profiles based on the ID List contained in the
 * FacebookConfiguration object
 */
public class FacebookPageProvider extends FacebookProvider {

    public FacebookPageProvider(FacebookConfiguration facebookConfiguration) {
        super(facebookConfiguration);
    }

    @VisibleForTesting
    BlockingQueue<StreamsDatum> getQueue() {
        return super.datums;
    }

    @Override
    protected FacebookDataCollector getDataCollector() {
        return new FacebookPageDataCollector(super.datums, super.configuration);
    }
}
