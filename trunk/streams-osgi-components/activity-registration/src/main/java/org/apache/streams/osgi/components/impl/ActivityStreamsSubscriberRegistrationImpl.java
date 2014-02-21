package org.apache.streams.osgi.components.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.streams.osgi.components.ActivityStreamsSubscriberRegistration;
import org.apache.streams.osgi.components.activitysubscriber.ActivityStreamsSubscription;
import org.apache.streams.osgi.components.activitysubscriber.impl.ActivityStreamsSubscriberDelegate;

public class ActivityStreamsSubscriberRegistrationImpl implements ActivityStreamsSubscriberRegistration {
    private static final transient Log LOG = LogFactory.getLog(ActivityStreamsSubscriberRegistrationImpl.class);
    private boolean verbose = true;
    private String prefix = "Activity Subscriber Registration";

    public Object register(Object body) {

        //authorize this subscriber based on some rule set...
        //create a new SubscriberDelegate...
        //using the URI supplied to set it up...
        //return the consumer for addition to the consumer warehouse

        ActivityStreamsSubscription configuration = (ActivityStreamsSubscription)body;

        ActivityStreamsSubscriberDelegate delegate =    new ActivityStreamsSubscriberDelegate(configuration);
        //authenticate
        delegate.setAuthenticated(true);



        return  delegate;
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
