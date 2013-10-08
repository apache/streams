package org.apache.streams.persistence.model.cassandra;

import org.apache.streams.persistence.model.ActivityStreamsSubscription;
import org.codehaus.jackson.map.annotate.JsonDeserialize;

import java.util.ArrayList;
import java.util.List;


public class CassandraSubscription implements ActivityStreamsSubscription {
    @JsonDeserialize(as=ArrayList.class)
    private List<String> filters;

    private String authToken;

    public void setFilters(List<String> filters) {
        //TODO: it's possible that this could be null
        this.filters = filters;
    }

    @Override
    public List<String> getFilters(){
        return filters;

    }

    @Override
    public String getAuthToken() {
        return authToken;
    }

    @Override
    public void setAuthToken(String auth_token) {
        this.authToken = auth_token;
    }
}
