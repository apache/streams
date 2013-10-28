package org.apache.streams.persistence.model;

import java.util.List;

public interface ActivityStreamsSubscription {

    void setFilters(List<String> filters);
    List<String> getFilters();

    String getId();
    void setId(String id);

    String getInRoute();
    void setInRoute(String inRoute);

}
