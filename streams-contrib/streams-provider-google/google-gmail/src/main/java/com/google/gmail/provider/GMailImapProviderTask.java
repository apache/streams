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

package com.google.gmail.provider;

import com.googlecode.gmail4j.GmailMessage;
import org.apache.streams.core.StreamsDatum;
import org.apache.streams.pojo.json.Activity;
import org.apache.streams.util.ComponentUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Created by sblackmon on 12/10/13.
 */
public class GMailImapProviderTask implements Runnable {

    private final static Logger LOGGER = LoggerFactory.getLogger(GMailImapProviderTask.class);

    private GMailProvider provider;

    public GMailImapProviderTask(GMailProvider provider) {
        this.provider = provider;
    }

    @Override
    public void run() {

        final List<GmailMessage> messages = this.provider.imapClient.getUnreadMessages();

        for (GmailMessage message : messages) {

            Activity activity;
            GMailMessageActivityConverter serializer = new GMailMessageActivityConverter( this.provider );
            activity = serializer.deserialize(message);
            StreamsDatum entry = new StreamsDatum(activity);
            ComponentUtils.offerUntilSuccess(entry, this.provider.providerQueue);

        }

    }


}
