package org.apache.streams.osgi.components.impl;

import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.streams.osgi.components.ActivityPublisherRegistration;
import org.apache.streams.osgi.components.activityconsumer.ActivityConsumer;
import org.apache.streams.osgi.components.activityconsumer.impl.PushActivityConsumer;

public class ActivityPublisherRegistrationImpl implements ActivityPublisherRegistration {
    private static final transient Log LOG = LogFactory.getLog(ActivityPublisherRegistrationImpl.class);
    private boolean verbose = true;
    private String prefix = "Activity Publisher Registration";

    public Object register(Object body) {

        //authorize this producer based on some rule set...
        //create a new ActivityConsumer...
        //using the URI supplied to set it up...
        //return the consumer for addition to the consumer warehouse

        String answer = prefix + " set body:  " + body + " " + new Date();
        LOG.info(">> call >>" + answer);


        ActivityConsumer activityConsumer =(ActivityConsumer)body;
        //authenticate..
        activityConsumer.setAuthenticated(true);
        return activityConsumer;
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
