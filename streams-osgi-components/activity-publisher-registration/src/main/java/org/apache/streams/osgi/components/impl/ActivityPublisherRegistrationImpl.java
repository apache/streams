package org.apache.streams.osgi.components.impl;

import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.streams.osgi.components.ActivityPublisherRegistration;

public class ActivityPublisherRegistrationImpl implements ActivityPublisherRegistration {
    private static final transient Log LOG = LogFactory.getLog(ActivityPublisherRegistrationImpl.class);
    private boolean verbose = true;
    private String prefix = "Activity Publisher Registration";

    public Object register(Object body) {
        String answer = prefix + " set body:  " + body + " " + new Date();
        if (verbose) {
            System.out.println(">> call >> " + answer);
        }
        LOG.info(">> call >>" + answer);
        return answer;
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
