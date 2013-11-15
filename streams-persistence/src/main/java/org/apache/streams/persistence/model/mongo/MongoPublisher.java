package org.apache.streams.persistence.model.mongo;

import com.mongodb.BasicDBObject;
import org.apache.streams.persistence.model.ActivityStreamsPublisher;

public class MongoPublisher extends BasicDBObject implements ActivityStreamsPublisher {

    @Override
    public String getInRoute() {
        return (String)get("inRoute");
    }

    @Override
    public String getId() {
        return (String)get("id");
    }

    @Override
    public String getSrc() {
        return (String)get("src");
    }

    @Override
    public void setInRoute(String inRoute) {
        put("inRoute", inRoute);
    }

    @Override
    public void setId(String id) {
        put("id", id);
    }

    @Override
    public void setSrc(String src) {
        put("src", src);
    }
}
