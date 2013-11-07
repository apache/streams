package org.apache.streams.persistence.model;

import java.util.Set;

public interface ActivityStreamsSubscription {

    void setTags(Set<String> tags);
    Set<String> getTags();

    String getInRoute();
    void setInRoute(String inRoute);

    String getUsername();
    void setUsername(String username);

}
