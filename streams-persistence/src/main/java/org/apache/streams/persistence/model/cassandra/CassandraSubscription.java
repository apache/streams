package org.apache.streams.persistence.model.cassandra;

import org.apache.streams.persistence.model.ActivityStreamsSubscription;
import org.codehaus.jackson.map.annotate.JsonDeserialize;

import java.util.HashSet;
import java.util.Set;


public class CassandraSubscription implements ActivityStreamsSubscription {
    @JsonDeserialize(as=HashSet.class)
    private Set<String> tags;

    private String username;
    private String inRoute;

    public void setTags(Set<String> tags) {
        //TODO: it's possible that this could be null
        this.tags = tags;
    }

    @Override
    public Set<String> getTags(){
        return tags;
    }

    @Override
    public String getInRoute() {
        return inRoute;
    }

    @Override
    public void setInRoute(String inRoute) {
        this.inRoute = inRoute;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public void setUsername(String username) {
        this.username = username;
    }
}
