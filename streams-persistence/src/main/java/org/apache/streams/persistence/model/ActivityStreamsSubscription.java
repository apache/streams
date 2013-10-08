package org.apache.streams.persistence.model;

import org.codehaus.jackson.annotate.JsonTypeInfo;

import java.util.List;

@JsonTypeInfo(use= JsonTypeInfo.Id.CLASS, include=JsonTypeInfo.As.PROPERTY, property="@class")
public interface ActivityStreamsSubscription {

    public void setFilters(List<String> filters);
    public List<String> getFilters();

    public String getAuthToken();
    public void setAuthToken(String token);

}
