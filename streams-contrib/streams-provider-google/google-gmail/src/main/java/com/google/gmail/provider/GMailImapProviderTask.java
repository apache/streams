package com.google.gmail.provider;

/*
 * #%L
 * google-gmail
 * %%
 * Copyright (C) 2013 - 2014 Apache Streams Project
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import com.googlecode.gmail4j.GmailClient;
import com.googlecode.gmail4j.GmailMessage;
import org.apache.streams.core.StreamsDatum;
import org.apache.streams.pojo.json.Activity;
import org.apache.streams.util.ComponentUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Calendar;
import java.util.GregorianCalendar;
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

        Calendar calendar = new GregorianCalendar();

        calendar.set(Calendar.YEAR, 2000);
        calendar.set(Calendar.MONTH, 0);
        calendar.set(Calendar.DAY_OF_MONTH, 0);
        calendar.set(Calendar.HOUR, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        final List<GmailMessage> messages = this.provider.imapClient.getMessagesBy(
                GmailClient.EmailSearchStrategy.DATE_GT,
                calendar.getTime().toString()
        );

        for (GmailMessage message : messages) {

            Activity activity;
            GMailMessageActivitySerializer serializer = new GMailMessageActivitySerializer( this.provider );
            activity = serializer.deserialize(message);
            StreamsDatum entry = new StreamsDatum(activity);
            ComponentUtils.offerUntilSuccess(entry, this.provider.providerQueue);

        }

    }


}
