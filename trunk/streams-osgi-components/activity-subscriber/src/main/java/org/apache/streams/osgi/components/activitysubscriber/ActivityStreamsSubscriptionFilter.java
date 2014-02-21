package org.apache.streams.osgi.components.activitysubscriber;

import org.codehaus.jackson.annotate.JsonTypeInfo;

@JsonTypeInfo(use= JsonTypeInfo.Id.CLASS, include=JsonTypeInfo.As.PROPERTY, property="@class")
public interface ActivityStreamsSubscriptionFilter {



    public void setQuery(String query);

    public String getQuery();

    public boolean evaluate(String activity);

}
