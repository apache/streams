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
