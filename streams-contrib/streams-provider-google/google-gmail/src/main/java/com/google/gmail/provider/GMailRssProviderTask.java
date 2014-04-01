package com.google.gmail.provider;

import com.googlecode.gmail4j.GmailMessage;
import org.apache.streams.core.StreamsDatum;
import org.apache.streams.util.ComponentUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Created by sblackmon on 12/10/13.
 */
public class GMailRssProviderTask implements Runnable {

    private final static Logger LOGGER = LoggerFactory.getLogger(GMailRssProviderTask.class);

    private GMailProvider provider;

    public GMailRssProviderTask(GMailProvider provider) {
        this.provider = provider;
    }

    @Override
    public void run() {

        final List<GmailMessage> messages = this.provider.rssClient.getUnreadMessages();
        for (GmailMessage message : messages) {

            StreamsDatum entry = new StreamsDatum(message);

            ComponentUtils.offerUntilSuccess(entry, this.provider.providerQueue);
        }

    }

}
