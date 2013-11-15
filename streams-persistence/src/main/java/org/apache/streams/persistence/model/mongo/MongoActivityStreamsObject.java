package org.apache.streams.persistence.model.mongo;

import com.mongodb.BasicDBObject;
import org.apache.streams.persistence.model.ActivityStreamsObject;

public class MongoActivityStreamsObject extends BasicDBObject implements ActivityStreamsObject{

    public String getDisplayName() {
        return (String)get("displayName");
    }

    public void setDisplayName(String displayName) {
        put("displayName", displayName);
    }

    public String getId() {
        return (String)get("id");
    }

    public void setId(String id) {
        put("id", id);
    }

    public String getObjectType() {
        return (String)get("objectType");
    }

    public void setObjectType(String objectType) {
        put("objectType", objectType);
    }

    public String getUrl() {
        return (String)get("url");
    }

    public void setUrl(String url) {
        put("url", url);
    }
}
