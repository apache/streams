package org.apache.streams.persistence.model;

import org.codehaus.jackson.annotate.JsonTypeInfo;

import java.util.List;

public interface ActivityStreamsSubscription {

    public void setFilters(List<String> filters);
    public List<String> getFilters();

    public String getId();
    public void setId(String id);

}
