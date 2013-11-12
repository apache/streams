package org.apache.streams.persistence.model;

import java.util.Set;

public interface ActivityStreamsSubscription {

    void setFilters(Set<String> tags);
    Set<String> getFilters();

    String getInRoute();
    void setInRoute(String inRoute);

    String getUsername();
    void setUsername(String username);

}
