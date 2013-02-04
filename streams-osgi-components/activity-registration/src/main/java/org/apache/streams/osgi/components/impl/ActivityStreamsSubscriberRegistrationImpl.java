package org.apache.streams.osgi.components.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.streams.osgi.components.ActivityStreamsSubscriberRegistration;
import org.apache.streams.osgi.components.activitysubscriber.ActivityStreamsSubscriberConfiguration;
import org.apache.streams.osgi.components.activitysubscriber.impl.ActivityStreamsSubscriberDelegate;

import java.util.Date;
import java.util.HashMap;

public class ActivityStreamsSubscriberRegistrationImpl implements ActivityStreamsSubscriberRegistration {
    private static final transient Log LOG = LogFactory.getLog(ActivityStreamsSubscriberRegistrationImpl.class);
    private boolean verbose = true;
    private String prefix = "Activity Subscriber Registration";

    public Object register(Object body) {

        //authorize this subscriber based on some rule set...
        //create a new SubscriberDelegate...
        //using the URI supplied to set it up...
        //return the consumer for addition to the consumer warehouse

        ActivityStreamsSubscriberConfiguration configuration = (ActivityStreamsSubscriberConfiguration)body;

        return new ActivityStreamsSubscriberDelegate(configuration);
    }



    public boolean isVerbose() {
        return verbose;
    }

    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }
}
