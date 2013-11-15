package org.apache.streams.persistence.model.mongo;


import com.mongodb.BasicDBObject;
import org.apache.streams.persistence.model.ActivityStreamsSubscription;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class MongoSubscription extends BasicDBObject implements ActivityStreamsSubscription {

    public MongoSubscription(){
        put("filters",new HashSet<String>());
    }

    @Override
    public void setFilters(Set<String> filters) {
        put("filters",filters);
    }

    @Override
    public Set<String> getFilters() {
        Collection filters = (Collection<String>)get("filters");

        if(filters instanceof Set){
            return (Set<String>)filters;
        }else{
            filters = new HashSet<String>(filters);
            put("filters",filters);
            return (Set<String>)filters;
        }
    }

    @Override
    public String getInRoute() {
        return (String)get("inRoute");
    }

    @Override
    public void setInRoute(String inRoute) {
        put("inRoute",inRoute);
    }

    @Override
    public String getUsername() {
        return (String)get("username");
    }

    @Override
    public void setUsername(String username) {
        put("username",username);
    }
}
